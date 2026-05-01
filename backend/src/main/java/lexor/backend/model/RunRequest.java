package lexor.backend.model;

public class RunRequest {
    private String code;
    private String input;

    public String getCode()  { return code; }
    public String getInput() { return input; }
    public void setCode(String code)   { this.code  = code; }
    public void setInput(String input) { this.input = input; }
}