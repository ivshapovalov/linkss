package ru.ivan.linkss.util;

public class Constants {

    //redis://localhost:6379/0
    public static final String REDIS_ONE_URL= System.getenv("REDIS_ONE_URL");

    //http://yoursite.com:8080/
    public static final String GEOIP_URL = System.getenv("GEOIP_URL");
    public static final String GEOIP_KEY = "GZHAAGAGASDGFASLJKjhdYIFDGHDZFGsdfsafaga";

    public static final String GOOGLE_SITE_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    public static final String GOOGLE_VERIFE_SECRET_KEY = "6LfYMRkUAAAAAAgpZsFa5NlQatQUpXKlDv6BQ6Kz";

    public static  final boolean DEBUG=true;
}
