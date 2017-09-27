import com.urbancode.anthill3.main.client.*
import com.urbancode.anthill3.domain.project.*

//Script In Development
class scriptV2{
    public static final String HOST = "anthillpro.int.thomson.com"
    public static final int PORT = 8080
    private static final String USER = 'UC229957'
    private static final String PASSWORD = 'AEto2AnthilPro'

    static void start (String[] args){
        AnthillClient client = AnthillClient.connect(HOST, PORT, USER, PASSWORD)


        System.console().println client
    }

}
