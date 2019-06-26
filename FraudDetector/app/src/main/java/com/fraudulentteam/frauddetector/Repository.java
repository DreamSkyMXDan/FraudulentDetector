package com.fraudulentteam.frauddetector;

public class Repository {

    private static Repository repository = null;
    public synchronized static Repository getInstance()
    {
        if (repository == null)
            repository = new Repository();

        return repository;
    }
    private byte[] checks = null;
    public void setCheck (byte[] checks) {
        this.checks = checks;
    }
    public byte[] getChecks () {
        return this.checks;
    }


}
