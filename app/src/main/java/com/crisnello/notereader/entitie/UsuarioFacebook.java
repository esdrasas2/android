package com.crisnello.notereader.entitie;

import java.io.Serializable;

/**
 * Created by crisnello on 08/05/17.
 */

public class UsuarioFacebook implements Serializable {

    private long id;
    private String name;
    private String email;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "ID :"+getId()+" Nome :"+getName()+" Email :"+getEmail();
    }
}
