import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection

class GetActGentVersionsForDeployment {
    public static final String SUCCESS = 'success'
    public static final String URL = 'http://anthillpro.int.thomson.com:8080'

    public static final String                    ACT_UI_GIT_WORKFLOW_ID = "44816"
    public static final String                GENT_PL_WF_GIT_WORKFLOW_ID = "44826"
    public static final String              GENT_FWNV_WF_GIT_WORKFLOW_ID = "44834"
    public static final String   WMET_FORM_AUTHORING_BACKEND_WORKFLOW_ID = "43611"
    public static final String  WMET_FORM_AUTHORING_FRONTEND_WORKFLOW_ID = "43614"
    public static final String              GENT_WMET_WF_GIT_WORKFLOW_ID = "45161"
    public static final String GENT_TXLAW_INTEGRATION_WF_GIT_WORKFLOW_ID = "45156"
    public static final String      WMET_PROCUREMENT_BACKEND_WORKFLOW_ID = "43617"
    public static final String     WMET_PROCUREMENT_FRONTEND_WORKFLOW_ID = "43620"
    public static final String                    ACR_UI_GIT_WORKFLOW_ID = "44713"
    public static final String                   GENT_NAA_WF_WORKFLOW_ID = "45839"
    public static final String                       LCRL_UI_WORKFLOW_ID = "47301"

    //class for working with clipboard
    static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()

    static void main(String[] args) {
        //Checking args
        if (args.size() < 2) {
            System.console().println "This script script requires the following arguments:\n" +
                    "Anthill Pro account username\n" +
                    "Anthill Pro account password\n" +
                    "optional: QA or PROD phrase"
            return
        }

        //String, which will be copied to clipboard
        StringBuilder result = new StringBuilder()

        //commented code need if you want to enter
//        def username = System.console().readLine 'Please, enter your login for AntHill Pro: '
        def username = args[0]
//        def password = System.console().readLine 'Enter your password for AntHill Pro: '
        def password = args[1]
//        def env = System.console().readLine 'Enter environment (PROD/QA): '
        def env = args[2]

        System.console().println("Hello, " + args[0])
        System.console().println "Selected " + env + " environment\n"

        //Setting credentials
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("$username", "$password".toCharArray());
            }
        })

        //Creating list of workflow id's for anthill pro
        ArrayList<String> WfIds = addArtifactIds()

        //For every workflow id
        WfIds.each { workflowID ->

            //rest url for getting List Workflow Build Lives
            def path = "/rest/workflow/$workflowID/buildlives"
            def connection = new URL(URL + path)
                    .openConnection() as HttpURLConnection

            if (connection.responseCode == 200) {
                // get the XML response
                def text = connection.inputStream.text
                def xml = new XmlParser().parseText(text)

                def versionTEST = '0' //that's only for LCRL-UI
                def versionUAT = '0'
                def versionQA = '0'
                def versionPROD = '0'
                def currentDate = '0'

                //checking every buildlifes for version and statuses (success\failure and environments
                xml.'buildlife'.each { buildlife ->
                    def buildId = buildlife.@'id'
                    ArrayList<String> statuses = buildlife.'statuses'.'status'.@status
                    ArrayList<String> date = buildlife.'statuses'.'status'.@date
                    def version = buildlife.'stamps'.'status'.@stamp[0]

//                    System.console().println date.get(statuses.indexOf('UAT-SiteA'))


                    if ((statuses.contains(SUCCESS))) {
                        if (((statuses.contains('UAT-SiteA')) && (statuses.contains('UAT-SiteB'))) ||
                                (statuses.contains('UAT'))) {
                            if (version >= versionUAT) {
                                versionUAT = version
                                currentDate = date
                            }
                        }
                        if (((statuses.contains('QA-SiteA')) && (statuses.contains('QA-SiteB'))) ||
                                (statuses.contains('QA'))) {
                            if (version >= versionQA) {
                                versionQA = version
                                currentDate = date
                            }
                        }
                        if ((statuses.contains('Prod-SiteA')) && (statuses.contains('Prod-SiteB'))) {
                            if (version >= versionPROD) {
                                versionPROD = version
                                currentDate = date
                            }
                        }
                        if (statuses.contains('Test')) {
                            if (version >= versionTEST) {
                                versionTEST = version
                                currentDate = date
                            }
                        }
                    }
                }

                println xml.attributes().get('workflowName')
                switch (env) {
                    case "QA":
                        if (workflowID == LCRL_UI_WORKFLOW_ID){
                            println checkVersions(versionTEST, versionQA)
                            result.append(checkVersions(versionTEST, versionQA)).append(System.lineSeparator())
                        } else {
                            println checkVersions(versionUAT, versionQA)
                            result.append(checkVersions(versionUAT, versionQA)).append(System.lineSeparator())
                        }
                        break
                    case "PROD":
                        if (workflowID == LCRL_UI_WORKFLOW_ID){
                            println checkVersions(versionTEST, versionPROD)
                            result.append(checkVersions(versionTEST, versionPROD)).append(System.lineSeparator())
                        } else {
                            println checkVersions(versionUAT, versionPROD)
                            result.append(checkVersions(versionUAT, versionPROD)).append(System.lineSeparator())
                        }
                        break
                    default:
                        println "UAT    : " + versionUAT
                        println "QA     : " + versionQA
                        println "PROD   : " + versionPROD
                        break
                }
                println()
            }
        }
        //copying result with versions to clipboard
        setClipboardContents(result.toString())
        System.console().println "Result was copied to clipboard, just ctrl+v on version column." +
                "\nHave a nice day!" +
                "\nP.S. Database change request isn't included."

    }

    private static boolean checkDate (){

        return false
    }

    private static ArrayList<String> addArtifactIds() {
        ArrayList<String> list = new ArrayList<>()

        //There are workflow ids for every project
        list.add(ACT_UI_GIT_WORKFLOW_ID)
        list.add(GENT_PL_WF_GIT_WORKFLOW_ID)
        list.add(GENT_FWNV_WF_GIT_WORKFLOW_ID)
        list.add(ACR_UI_GIT_WORKFLOW_ID)
        list.add(WMET_FORM_AUTHORING_BACKEND_WORKFLOW_ID)
        list.add(WMET_FORM_AUTHORING_FRONTEND_WORKFLOW_ID)
        list.add(GENT_WMET_WF_GIT_WORKFLOW_ID)
        list.add(GENT_TXLAW_INTEGRATION_WF_GIT_WORKFLOW_ID)
        list.add(WMET_PROCUREMENT_BACKEND_WORKFLOW_ID)
        list.add(WMET_PROCUREMENT_FRONTEND_WORKFLOW_ID)
        list.add(GENT_NAA_WF_WORKFLOW_ID)
//        list.add(LCRL_UI_WORKFLOW_ID)

        return list
    }

    private static String checkVersions(String versionUAT, String anotherVersion) {
        if (anotherVersion == versionUAT) {
            return "No Changes"
        } else if (anotherVersion < versionUAT) {
            return versionUAT
        } else {
            return "Something weird happens, please check version by yourself"
        }
    }

    static void setClipboardContents(final String contents) {
        clipboard.setContents(new StringSelection(contents), null)
    }
}