import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;



public class xmlParser{
    void reader(String FileName, String OutFile) throws ParserConfigurationException, IOException, SAXException {
        File inputFile = new File(FileName);
        //generate random seed
        Random r = new Random();
        //create the csv file
        FileWriter outputfile = new FileWriter(OutFile);
        //create the csv writer
        PrintWriter printWriter = new PrintWriter(outputfile);

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("timestep");

        //System.out.println("Number of elements"+nList.getLength());
        for(int temp = 0; temp < nList.getLength();temp++){
            Node nNode = nList.item(temp);
            NodeList childNodes = nNode.getChildNodes();
            NamedNodeMap currAttributes = nNode.getAttributes();
            String timestepVal = "";

            for (int j = 0 ; j < currAttributes.getLength();j++){
                Node currNode = currAttributes.item(j);
                if(currNode.getNodeType() == Node.ATTRIBUTE_NODE){
                    timestepVal = currNode.getNodeValue();

                }
            }

            //parse the vehicles for the current timestep

            
            for(int j = 0; j < childNodes.getLength();j++){
                Node myNode = childNodes.item(j);
                String id="";
                String lat="";
                String longt="";
                String angle="";
                String speed="";
                int rssiInt = r.nextInt(81)+20;
                String RSSI = Integer.toString(rssiInt);
                double throughputDouble = (rssiInt/100.0)*(50);
                String throughput=Double.toString(throughputDouble);
                if (myNode.getNodeType()==Node.ELEMENT_NODE){
                    //initialize the string for the output file
                    String outputString = "<"+timestepVal+", ";
                    NamedNodeMap attributes = myNode.getAttributes();
                    for(int i = 0 ; i <attributes.getLength() ;i++){
                        Node item = attributes.item(i);
                        if (item.getNodeName().equals("id")){
                            id = item.getNodeValue();
                        }
                        else if (item.getNodeName().equals("angle")){
                            angle = item.getNodeValue();
                        }
                        else if (item.getNodeName().equals("speed")){
                            speed = item.getNodeValue();
                        }
                        else if (item.getNodeName().equals("x")){
                            longt = item.getNodeValue();
                        }
                        else if (item.getNodeName().equals("y")){
                            lat = item.getNodeValue();
                        }
                    }

                    //create the string to write to the file
                    outputString += id + ", " + lat +", " + longt + ", " + angle  +", "+speed+", "+ RSSI+", "+throughput+">";
                    //System.out.println("Output String: "+outputString);
                    printWriter.println(outputString);
                }
            }


        }
        printWriter.close();

    }

    List<String[]> csvParser(String fileName) throws IOException {
        //list to save the data
        List<String[]> collectiveData = new LinkedList<String[]>();
        BufferedReader csvReader = new BufferedReader(new FileReader(fileName));
        String[] data = new String[0];
        String row;
        while ((row = csvReader.readLine()) != null) {
            data = row.split(",");
            // do something with the data
            data[0] = data[0].substring(1);
            data[data.length-1] = data[data.length-1].substring(0,data[data.length-1].length()-1);

            collectiveData.add(data);

        }
        csvReader.close();
        return collectiveData;
    }
}


