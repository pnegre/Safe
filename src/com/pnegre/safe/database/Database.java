package com.pnegre.safe.database;

import java.util.List;

/**
 * User: pnegre
 * Date: 05/10/12
 * Time: 22:44
 */
public interface Database {

    void destroy();

    boolean ready();

    List getSecrets();

    void newSecret(Secret s);

    Secret getSecret(int id);

    void deleteSecret(int id);

    void updateSecret(Secret s);

    void wipe();

}
