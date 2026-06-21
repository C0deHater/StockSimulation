package ssg.dto;

public class UserRequest {
    private String username;
    private String password;
    private Long targetMoney;

    public UserRequest(String username, String password, Long targetMoney) {
        this.username = username;
        this.password = password;
        this.targetMoney = targetMoney;
    }
}
