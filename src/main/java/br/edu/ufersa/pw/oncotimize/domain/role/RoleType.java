package br.edu.ufersa.pw.oncotimize.domain.role;

import lombok.Getter;

@Getter
public enum RoleType {
    ROLE_USER("USER"),
    ROLE_ADMIN("ADMIN");

    private final String name;

    RoleType(String simpleName) {
        this.name = simpleName;
    }
}
