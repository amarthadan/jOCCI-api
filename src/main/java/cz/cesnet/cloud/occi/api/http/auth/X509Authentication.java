package cz.cesnet.cloud.occi.api.http.auth;

import cz.cesnet.cloud.occi.api.Authentication;

public class X509Authentication extends HTTPAuthentication {

    private static final String IDENTIFIER = "OCCIX509Authentication";
    private String certificate;
    private String password;
    private String CAPath;
    private String CAFile;

    public X509Authentication(String certificate, String password) {
        this(certificate, password, null, null);
    }

    public X509Authentication(String certificate, String password, String CAPath, String CAFile) {
        this.certificate = certificate;
        this.password = password;
        this.CAPath = CAPath;
        this.CAFile = CAFile;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCAPath() {
        return CAPath;
    }

    public void setCAPath(String CAPath) {
        this.CAPath = CAPath;
    }

    public String getCAFile() {
        return CAFile;
    }

    public void setCAFile(String CAFile) {
        this.CAFile = CAFile;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Authentication getFallback() {
        return new KeystoneAuthentication(this);
    }

    @Override
    public void authenticate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
