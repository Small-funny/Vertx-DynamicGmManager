package Server.Automation;

import org.jdom2.Element;

public class NewXmlMapping {

    public static String createElementString(Element element, String route) {
        String authorization = element.getAttributeValue("authorization");
        String pageType = element.getAttributeValue("type");
        StringBuilder stringBuilder = new StringBuilder();
        for(Element child : element.getChildren()){
            elementForm(child);
        }

        return stringBuilder.toString();
    }
    private static String elementForm(Element element){
        StringBuilder stringBuilder = new StringBuilder();
        for (Element child :element.getChildren()){
            switch (child.getName()){
                case "input":
                    elementInput(child);
                    break;
                case "select":
                    elementSelect(child);
                    break;
                case "formcheck":
                    elementFormCheck(child);
                    break;
                default:
                    break;

            }
            stringBuilder.append(child);
        }
        return stringBuilder.toString();

    }
    private static String elementInput(Element element){
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder.toString();
    }
    private static String elementSelect(Element element){
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder.toString();
    }
    private static String elementFormCheck(Element element){
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder.toString();
    }
}
