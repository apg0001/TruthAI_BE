//package jpabasic.truthaiserver.service.sources;
//
//import jpabasic.truthaiserver.dto.sources.SourcesDto;
//
//import java.util.regex.Pattern;
//import java.util.*;
//import java.util.stream.Collectors;
//
//public class SourceParser {
//
//    // 자주 쓰는 패턴들
//    private static final Pattern P_MD     = Pattern.compile("^[-*]\\s*\\[(.+?)]\\((https?://[^\\s)]+)\\)\\s*$"); // - [Title](URL)
//    private static final Pattern P_COLON  = Pattern.compile("^[-*]?\\s*([^:]+?)\\s*:\\s*(https?://\\S+)\\s*$"); // - Title: URL
//    private static final Pattern P_DASH   = Pattern.compile("^[-*]?\\s*([^\\-]+?)\\s*-+\\s*(https?://\\S+)\\s*$"); // - Title - URL
//    private static final Pattern P_MDASH  = Pattern.compile("^[-*]?\\s*([^—]+?)\\s*—+\\s*(https?://\\S+)\\s*$"); // - Title — URL
//    private static final Pattern P_PAREN  = Pattern.compile("^\\s*(.+?)\\s*\\((https?://[^\\s)]+)\\)\\s*$");      // Title (URL)
//    private static final Pattern P_ANGLE  = Pattern.compile("^\\s*(.+?)\\s*[:：]\\s*<\\s*(https?://\\S+?)\\s*>\\s*$"); // Title: <URL>
//    private static final Pattern P_SPACE  = Pattern.compile("^\\s*(.+?)\\s+(https?://\\S+)\\s*$");                // Title URL
//
//    public static List<SourcesDto> parse(String sourceText){
//        if(sourceText==null || sourceText.isBlank()) return List.of();
//
//        String normalized=sourceText
//                .replace("\r\n","\n")
//                .replace("\r","\n")
//                .replace("\t"," ")
//                .trim();
//
//        String [] lines = normalized.split("\n");
//        List<SourcesDto> list=new ArrayList<>();
//
//        int lineNo = 0;
//        for (String raw : lines) {
//            lineNo++;
//            String line = raw.trim();
//            if (line.isEmpty()) continue;
//
//            String lower = line.toLowerCase();
//            if (lower.equals("sources") || lower.startsWith("## sources")) continue;
//
//            SourcesDto dto =
//                    tryExtract(line);
//            if (dto != null) {
//                list.add(dto);
//            }
//        }
//
//        //URL 기준 중복 제거 + 길이 제한
//        List<SourcesDto> distinct=list.stream()
//                .map(s->new SourcesDto(cleanTitle(s.title()),cleanUrl(s.url())))
//                .collect(Collectors.collectingAndThen(
//                        Collectors.toMap(SourceDto::url,s->s,)
//                ))
//
//    }
//}
