package com.pnegre.safe.database;

/**
 * User: pnegre
 * Date: 05/10/12
 * Time: 22:44
 */

public class Secret implements Comparable {
    public String name;
    public String username;
    public String password;
    public int id;

    public Secret(int id, String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.id = id;
    }

    public Secret(Secret s) {
        name = s.name;
        username = s.username;
        password = s.password;
        id = s.id;
    }

    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Object o) {
        Secret s = (Secret) o;
        return name.compareTo(s.name);
    }

    @Override
    public boolean equals(Object o) {
        Secret s = (Secret) o;
        return s.name.equals(this.name);
    }
}
