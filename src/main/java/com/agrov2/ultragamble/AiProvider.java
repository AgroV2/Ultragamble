package com.agrov2.ultragamble;


import java.util.List;

public enum AiProvider {
    ANYMODEL("API1", "AI_TOKEN", "https://anymodel.org/v1/chat/completions",
            List.of("cx/gpt-5.6-sol", "cc/claude-opus-4-8", "cx/gpt-5.5", "cc/claude-sonnet-5",
                    "am/glm-5.2", "am/deepseek-v4-pro", "cx/gpt-5.6-luna", "ag/gemini-3-flash")),
    AGENTROUTER("API2", "AI_TOKEN2", "https://agentrouter.org/v1/chat/completions",
            List.of("claude-opus-4-6", "claude-opus-4-7", "claude-opus-4-8", "glm-5.2", "gpt-5.5")),
    HELPCODER("API3", "AI_TOKEN3", "https://helpcoder.cc/v1/chat/completions",
            List.of("gpt-5.1"));

    private final String displayName;
    private final String envKey;
    private final String url;
    private final List<String> models;

    AiProvider(String displayName, String envKey, String url, List<String> models) {
        this.displayName = displayName;
        this.envKey = envKey;
        this.url = url;
        this.models = models;
    }

    public String getDisplayName() { return displayName; }
    public String getUrl() { return url; }
    public List<String> getModels() { return models; }
    public String getToken() { return System.getenv(envKey); }
    public String getDefaultModel() { return models.get(0); }

    public static AiProvider fromName(String name) {
        if (name == null) return ANYMODEL;
        try { return valueOf(name); } catch (IllegalArgumentException e) { return ANYMODEL; }
    }
}