package com.gokalp.psylog_api.dto.response;

public class AuthResponse {

    public static class UserInfo {
        private Long id;
        private String email;
        private String role;

        public UserInfo(Long id, String email, String role) {
            this.id = id;
            this.email = email;
            this.role = role;
        }

        public Long getId() { return id; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
    }
}
