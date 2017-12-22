package com.suyong.kakaobot;

public class Type {
    public static class Message {
        public String room;
        public String sender;
        public String message;

        @Override
        public String toString() {
            return "Message{" +
                    "message='" + message + '\'' +
                    ", room='" + room + '\'' +
                    ", sender='" + sender + '\'' +
                    '}';
        }
    }

    public static class Project {
        public ProjectType type;
        public String title;
        public String subtitle;
        public boolean enable;

        @Deprecated
        public String isError;
    }

    public enum ProjectType {
        JS("js"),
        PYTHON("py");

        private String type;
        ProjectType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }
}
