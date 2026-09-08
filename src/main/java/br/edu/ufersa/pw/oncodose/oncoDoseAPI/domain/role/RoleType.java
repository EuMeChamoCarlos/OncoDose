package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.role;

public enum RoleType {
    ROLE_USER("USER"),
    ROLE_ADMIN("ADMIN");

    private final String name;

    RoleType(String simpleName) {
        this.name = simpleName;
    }
}
