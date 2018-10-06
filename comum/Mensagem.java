package comum;


//Esta classe é uma contribuicao do GUILHERME COPPINI

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mensagem implements Serializable {

    private String command;

    private List<Serializable> arguments;

    public Mensagem(String command, Serializable... arguments) {
        this.command = command;
        this.arguments = new ArrayList<>();
        Collections.addAll(this.arguments, arguments);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<Serializable> getArguments() {
        return arguments;
    }

    public void setArguments(List<Serializable> arguments) {
        this.arguments = arguments;
    }
}
