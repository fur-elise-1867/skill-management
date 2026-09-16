package com.furelise.skillmanagement.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ContentSecurityScanService {

    public record SecurityScanResult(
            boolean hasRisk,
            String detailMessage
    ) {}

    private record Rule(String name, Pattern pattern, String description) {}

    private final List<Rule> rules = List.of(
            new Rule("DESTRUCTIVE_COMMAND",
                    Pattern.compile("\\brm\\s+-[a-zA-Z]*r[a-zA-Z]*f\\b", Pattern.CASE_INSENSITIVE),
                    "Phát hiện lệnh xóa đệ quy có tính hủy hoại hệ thống (rm -rf)"),
            new Rule("PIPED_REMOTE_EXECUTION",
                    Pattern.compile("(curl|wget)\\s+[^|\\n]+\\|\\s*(bash|sh|python|zsh)", Pattern.CASE_INSENSITIVE),
                    "Phát hiện hành vi tải và thực thi script trực tiếp qua pipe (curl/wget | sh)"),
            new Rule("PRIVATE_KEY_EXPOSURE",
                    Pattern.compile("-----BEGIN [A-Z ]*PRIVATE KEY-----"),
                    "Phát hiện private key / cryptographic key bị hardcode trong nội dung"),
            new Rule("AWS_ACCESS_KEY",
                    Pattern.compile("\\bAKIA[0-9A-Z]{16}\\b"),
                    "Phát hiện AWS Access Key ID trong nội dung"),
            new Rule("GITHUB_TOKEN",
                    Pattern.compile("\\bghp_[A-Za-z0-9_]{36}\\b"),
                    "Phát hiện GitHub Personal Access Token"),
            new Rule("DANGEROUS_EVAL",
                    Pattern.compile("\\b(eval|exec)\\s*\\(", Pattern.CASE_INSENSITIVE),
                    "Phát hiện sử dụng hàm thực thi mã động nguy hiểm (eval/exec)"),
            new Rule("SUBPROCESS_SHELL_TRUE",
                    Pattern.compile("subprocess\\.(Popen|run|call)\\s*\\(.*shell\\s*=\\s*True", Pattern.CASE_INSENSITIVE),
                    "Phát hiện thực thi shell command nguy hiểm (shell=True trong subprocess)"),
            new Rule("OS_SYSTEM",
                    Pattern.compile("\\bos\\.system\\s*\\(", Pattern.CASE_INSENSITIVE),
                    "Phát hiện gọi trực tiếp lệnh hệ điều hành (os.system)")
    );

    public SecurityScanResult scan(String fileName, String content) {
        if (content == null || content.isBlank()) {
            return new SecurityScanResult(false, null);
        }

        List<String> detectedRisks = new ArrayList<>();

        for (Rule rule : rules) {
            if (rule.pattern.matcher(content).find()) {
                detectedRisks.add(rule.name + ": " + rule.description);
            }
        }

        if (detectedRisks.isEmpty()) {
            return new SecurityScanResult(false, null);
        }

        String details = String.join("; ", detectedRisks);
        return new SecurityScanResult(true, details);
    }
}
