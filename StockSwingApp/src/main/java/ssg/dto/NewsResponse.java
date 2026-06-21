package ssg.dto;

public class NewsResponse {
    private String date;
    private String title;
    private String type;
    private String url;
    private String corpName;
    private String source;

    public String getDate()     { return date     != null ? date     : ""; }
    public String getTitle()    { return title    != null ? title    : ""; }
    public String getType()     { return type     != null ? type     : ""; }
    public String getUrl()      { return url      != null ? url      : ""; }
    public String getCorpName() { return corpName != null ? corpName : ""; }
    public String getSource()   { return source   != null ? source   : ""; }
}
