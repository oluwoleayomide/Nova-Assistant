package com.nova.assistant.commands;

import java.util.Locale;

public class CommandParser {

    public enum CommandType {
        OPEN_APP,
        CALL_CONTACT,
        SEND_SMS,
        SET_ALARM,
        SET_TIMER,
        FLASHLIGHT_ON,
        FLASHLIGHT_OFF,
        LOCK_SCREEN,
        VOLUME_UP,
        VOLUME_DOWN,
        MUTE,
        PLAY_MUSIC,
        SCHEDULE_TASK,
        UNKNOWN
    }

    public static class ParsedCommand {
        public CommandType type;
        public String      target;   // app name / contact name / time string
        public String      body;     // SMS body / timer label
        public long        delayMs;  // for scheduled tasks

        public ParsedCommand(CommandType type, String target, String body, long delayMs) {
            this.type    = type;
            this.target  = target;
            this.body    = body;
            this.delayMs = delayMs;
        }
    }

    public static ParsedCommand parse(String input) {
        if (input == null) return unknown();
        String s = input.toLowerCase(Locale.US).trim();

        // --- OPEN APP ---
        if (s.startsWith("open ") || s.startsWith("launch ") || s.startsWith("start ")) {
            String app = s.replaceFirst("^(open|launch|start)\\s+", "").trim();
            return new ParsedCommand(CommandType.OPEN_APP, app, null, 0);
        }

        // --- CALL ---
        if (s.startsWith("call ") || s.startsWith("phone ") || s.startsWith("ring ")) {
            String contact = s.replaceFirst("^(call|phone|ring)\\s+", "").trim();
            return new ParsedCommand(CommandType.CALL_CONTACT, contact, null, 0);
        }

        // --- SMS ---
        if (s.startsWith("send") && (s.contains(" message") || s.contains(" sms") || s.contains(" text"))) {
            // "send message to John: I'm on my way"
            String after = s.replaceFirst("^send(\\s+a)?(\\s+message|\\s+text|\\s+sms)?\\s+to\\s+", "").trim();
            int colonIdx = after.indexOf(":");
            if (colonIdx > 0) {
                String contact = after.substring(0, colonIdx).trim();
                String body    = after.substring(colonIdx + 1).trim();
                return new ParsedCommand(CommandType.SEND_SMS, contact, body, 0);
            }
            return new ParsedCommand(CommandType.SEND_SMS, after, null, 0);
        }

        // --- ALARM ---
        if (s.contains("alarm") && (s.contains("set") || s.contains("wake"))) {
            String time = extractTime(s);
            return new ParsedCommand(CommandType.SET_ALARM, time, null, 0);
        }

        // --- TIMER ---
        if (s.contains("timer") || s.contains("remind me in")) {
            long ms = extractDurationMs(s);
            return new ParsedCommand(CommandType.SET_TIMER, null, null, ms);
        }

        // --- FLASHLIGHT ---
        if (s.contains("flashlight") || s.contains("torch")) {
            if (s.contains("off") || s.contains("turn off"))
                return new ParsedCommand(CommandType.FLASHLIGHT_OFF, null, null, 0);
            return new ParsedCommand(CommandType.FLASHLIGHT_ON, null, null, 0);
        }

        // --- LOCK ---
        if (s.contains("lock") && s.contains("screen")) {
            long delay = extractDurationMs(s);
            return new ParsedCommand(CommandType.LOCK_SCREEN, null, null, delay);
        }

        // --- VOLUME ---
        if (s.contains("volume up") || s.contains("turn up"))
            return new ParsedCommand(CommandType.VOLUME_UP, null, null, 0);
        if (s.contains("volume down") || s.contains("turn down"))
            return new ParsedCommand(CommandType.VOLUME_DOWN, null, null, 0);
        if (s.contains("mute") || s.contains("silent"))
            return new ParsedCommand(CommandType.MUTE, null, null, 0);

        // --- PLAY MUSIC ---
        if (s.startsWith("play ")) {
            String target = s.replaceFirst("^play\\s+", "").trim();
            // "play Burna Boy on Spotify in 10 minutes"
            long delay = extractDurationMs(s);
            String app = "spotify";
            if (s.contains(" on ")) {
                app = s.substring(s.lastIndexOf(" on ") + 4).trim();
                if (app.contains(" in ")) app = app.substring(0, app.indexOf(" in ")).trim();
            }
            if (delay > 0)
                return new ParsedCommand(CommandType.SCHEDULE_TASK, app, "play " + target, delay);
            return new ParsedCommand(CommandType.PLAY_MUSIC, app, target, 0);
        }

        return unknown();
    }

    // ---- Helpers ----

    private static String extractTime(String s) {
        // Tries to pull "6am", "7:30pm", "6 am" etc.
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
            "(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher m = p.matcher(s);
        if (m.find()) return m.group();
        return s;
    }

    private static long extractDurationMs(String s) {
        long ms = 0;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
            "(\\d+)\\s*(second|minute|hour|sec|min|hr)s?", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher m = p.matcher(s);
        while (m.find()) {
            long val  = Long.parseLong(m.group(1));
            String unit = m.group(2).toLowerCase(Locale.US);
            if (unit.startsWith("sec")) ms += val * 1000L;
            else if (unit.startsWith("min")) ms += val * 60_000L;
            else if (unit.startsWith("hour") || unit.startsWith("hr")) ms += val * 3_600_000L;
        }
        return ms;
    }

    private static ParsedCommand unknown() {
        return new ParsedCommand(CommandType.UNKNOWN, null, null, 0);
    }
}
