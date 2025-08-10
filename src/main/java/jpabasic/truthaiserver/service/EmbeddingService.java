package jpabasic.truthaiserver.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;
import org.openkoreantext.processor.KoreanTokenJava;
import scala.collection.Seq;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class EmbeddingService {

    @Value("${embedding.vec-path}")
//    private String vecPath = "models/ko-emb.vec";
    private String vecPath;

    // 필요시 강제 차원 지정(옵션). 지정 안 하면 파일에서 자동으로 추론
    @Value("${embedding.dim:0}")
    private int forcedDim;

    // 토큰 → 벡터
    private final Map<String, float[]> tokenVectors = new ConcurrentHashMap<>();
    private int dim = -1;

    // 문장 캐시
    private final Map<String, float[]> sentenceCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        String path = vecPath;
        if (path == null || path.isBlank()) {
            path = "classpath:models/ko-emb.vec"; // 기본값 (원하면 yml에 명시)
        }

        try (BufferedReader br = openReader(path)) {
            String line = br.readLine();
            if (line == null) throw new IllegalArgumentException("Empty vec file: " + path);

            String[] first = line.trim().split("\\s+");
            boolean hasHeader = first.length == 2 && isInt(first[0]) && isInt(first[1]);
            if (hasHeader) {
                dim = Integer.parseInt(first[1]);
                line = br.readLine();
            }

            int loaded = 0;
            while (line != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length < 2) { line = br.readLine(); continue; }

                String token = parts[0];
                int d = parts.length - 1;

                if (dim <= 0) dim = d;       // 헤더 없으면 라인으로 추론
                if (forcedDim > 0) dim = forcedDim; // 강제 차원 지정 시 덮어쓰기
                if (d != dim) { line = br.readLine(); continue; }

                float[] v = new float[dim];
                for (int i = 0; i < dim; i++) v[i] = Float.parseFloat(parts[i + 1]);
                tokenVectors.put(token, v);
                loaded++;
                line = br.readLine();
            }

            if (dim <= 0 || loaded == 0) {
                throw new IllegalStateException("No vectors loaded from " + path);
            }
            log.info("Loaded {} token vectors (dim={}) from {}", loaded, dim, path);

        } catch (Exception e) {
            log.error("Failed to load .vec from {}", path, e);
            throw new RuntimeException(e);
        }
    }

    private BufferedReader openReader(String path) throws Exception {
        if (path.startsWith("classpath:")) {
            String p = path.substring("classpath:".length());
            InputStream is = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream(p);
            if (is == null) throw new FileNotFoundException("classpath resource not found: " + p);
            return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        } else if (path.startsWith("file:")) {
            Path p = Paths.get(URI.create(path));
            return Files.newBufferedReader(p, StandardCharsets.UTF_8);
        } else {
            // 일반 파일 경로
            return Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8);
        }
    }

    public int getDim() { return dim; }

    /** 문장 → 임베딩(float[]) */
    public float[] embed(String text) {
        if (text == null || text.isBlank()) return zeros(dim);

        return sentenceCache.computeIfAbsent(text, t -> {
            List<String> toks = tokenizeKorean(t);
            List<float[]> vecs = new ArrayList<>(toks.size());
            for (String tk : toks) {
                float[] v = tokenVectors.get(tk);
                if (v != null) vecs.add(v);
            }
            if (vecs.isEmpty()) return zeros(dim);
            return mean(vecs, dim);
        });
    }

    /** 코사인 유사도 */
    public static double cosine(float[] a, float[] b) {
        int n = Math.min(a.length, b.length);
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < n; i++) { dot += a[i]*b[i]; na += a[i]*a[i]; nb += b[i]*b[i]; }
        return dot / (Math.sqrt(na) * Math.sqrt(nb) + 1e-12);
    }

    private static float[] mean(List<float[]> vecs, int dim) {
        float[] m = new float[dim];
        for (float[] v : vecs) for (int i = 0; i < dim; i++) m[i] += v[i];
        float inv = 1f / vecs.size();
        for (int i = 0; i < dim; i++) m[i] *= inv;
        return m;
    }

    private static float[] zeros(int dim) { return new float[Math.max(dim, 0)]; }

    private static boolean isInt(String s) { try { Integer.parseInt(s); return true; } catch (Exception e) { return false; } }

    /** 한국어 정규화 → 토큰화 → 어간화 → 조사/기호 제거(간단 필터) */
    private static List<String> tokenizeKorean(String text) {
        if (text == null || text.isBlank()) return List.of();

        // 1) 정규화
        CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);

        // 2) 토크나이즈 (stem 제거)ㄴ
        Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);
        List<KoreanTokenJava> list = OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens);

        // 3) 조사/어미/기호/파티클 제거 후 텍스트만 추출
        List<String> out = new ArrayList<>(list.size());
        for (KoreanTokenJava tk : list) {
            String pos = tk.getPos().toString();
            if (pos.equals("Josa") || pos.equals("Eomi") || pos.equals("Punctuation") || pos.equals("KoreanParticle")) {
                continue;
            }
            String s = tk.getText();
            if (s != null && !s.isBlank()) {
                out.add(s);
            }
        }
        return out;
    }

    @PreDestroy
    public void close() {
        sentenceCache.clear();
        tokenVectors.clear();
    }
}