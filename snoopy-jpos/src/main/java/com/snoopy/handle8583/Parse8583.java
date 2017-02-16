package com.snoopy.handle8583;

/**
 * Created by hnair20160706 on 2017/2/16.
 */

        import java.io.*;
        import java.util.ArrayList;
        import java.util.Collections;
        import java.util.Comparator;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;
        import java.util.Properties;
        import java.util.Set;
        import java.util.logging.Logger;

/**
 * @description：
 * 准备包含8583报文头、报文类型标识、位图、报文体各域的ISO8583metadata.xml配置文件
 * 准备8583十六进制报文
 * 使用SAXReader读取ISO8583metadata.xml文件，将文件中的内容解析成Map<String,Properties>
 * 使用文件输入流读取8583十六进制报文到字节数组输出流，字节数组输出流转换为字节数组
 * 将字节数组转换成字符串，此刻字符串的内容与十六进制里的内容完全一致，并将字符串换行、空去掉
 * 将字符串转换成字节数组（即将十六进制转换成十进制的字节数组）
 * 解析报文头（根据ISO8583metadata.xml中：isBCD确定编码、length确定长度、encoding确定编码、name作为标签名。现根据长度截取，再判断isBCD编码，根据相应的编码解码。）
 * 解析报文类型标识（根据长度，byte子数组，根绝对应的encoding编码进行解码）
 * 解析位图（判断第一个字节的二进制最高位是否为1，为1则使用扩展位图，为0则不使用扩展位图；根据长度获取byte字数组，转换成对应的二进制；根据二进制判断存在哪些域有值）
 * 解析报文体（将存在的域循环进行处理：判断是否变长，如果变长，先获取变长所表示的长度值，比如n..11(LLVAR)为两位变长，有两个字节表示长度，先拿两个字节计算本域所占几个字节，再获取相应字节数，再根据encoding编码进行解码；如果非变长，直接根据length获取长度，再根据encoding编码进行解码）
 * 将解析完成的8583报文信息所在的Map排序，便于打印阅览（此处不再说明，看代码即可）
 *
 * @warn注意点
 * 对于0~9的数字
 *         十六进制转换成十进制，相应于BCD码转换成十进制
 *         一个十六进制相当于一个byte，相当于两个[0，9]
 *
 * @see
 * 8583报文拆组包关键点：
 *     报文头各域、表问类型标识、位图或者报文体域所使用的编码方式，比如BCD编码还是普通的十六进制
 *     位图的使用
 *     报文体域的变长处理
 *
 * @see拆组包8583报文需要对于编码和解码、进制转换、字符集有一个充分和系统的了解
 */
public class Parse8583 {
    static int currentIndex = 0;
    static Map<String, Properties> map = Bean8583Factory.getInstance().getMap();
    public static void main(String[] args) throws UnsupportedEncodingException {
        //获取8583报文
//        byte[] resp = FileTools.readContent("D:/workspace/springweb/src/com/handle8583/8583resp.txt");
        byte[] resp = null;
        try {
            FileInputStream inputStream = new FileInputStream("E:\\MyWorkSpaces\\jpos\\snoopy-jpos\\src\\main\\resources\\iso8583\\iso8583.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb = new StringBuffer();
            String lintext=null;
            while ((lintext=bufferedReader.readLine())!=null){
                sb.append(lintext);
            }
            resp = sb.toString().getBytes();



        } catch (Exception e) {
            e.printStackTrace();
        }

        String respStr = new String(resp);
        /**消费*/
//        String hexStr = "";
        String hexStr = "303538322e02303538323030303430303030202020303030313030303020202031000001303030303030303000303030303030323030f23e44c1b8e09818000000001000008131363632303035383531313131373036343130303030303030303030303030313030303030353130303934313130303137313130303934313130303531303235313230343034373031313032313030303630383030303130303035303830303031303030353336393535383939303030303030303030303030313d303530393536373839303132333435363039393939393535383939303030303030303030303030313d313536313536303030303030303030303030303030333937363939393233363030303030323037303030303030303030303030303030303030303030303d3030303030303030303030303d303030303030303030313731313046343131323334354634323031323334353637383931324634337a68616e676a69616e67736f6674776172657061726b202020202020202020202020202020313536796bec80728a8a5a32303030303030303030303030303030303237303030302d303030303330303030303030303030303031313030353037363031303030303030303030303030303930303330202020204355503435342020435550414d30303130303130313130303030303030303036d5c4b4f3c7c53031313133383131313131313131303830303034303030303034333531365332323030303030343530303034303030302020203030303030303030303030303030303030303032433839344235440a5b393a35303a31353a3831303430375d5b3035313520303531355d0a";
        byte[] resp1 = hex2byte(hexStr);
        String hexStrtest = bytes2Hex(resp1);
        System.out.println(hexStrtest.equalsIgnoreCase(hexStr));
        String strresp1 = new String(resp1);
        byte[] strresp1bytes = strresp1.getBytes();
        String strresp1byteshex = bytes2Hex(strresp1bytes);
        if (strresp1byteshex.equalsIgnoreCase(hexStr)){
            System.out.println("正确转换");
        }
        System.out.println(strresp1);
        String strs1 = strresp1;
        String strs ="0582.\u0002058200040000   00010000   1\u0000\u0000\u000100000000\u0000000000200�>D����\u0018\u0000\u0000\u0000\u0000\u0010\u0000\u0000�16620058511117064100000000000001000005100941100171100941100510251204047011021000608000100050800010005369558990000000000001=0509567890123456099999558990000000000001=1561560000000000000003976999236000002070000000000000000000000=000000000000=00000000017110F4112345F42012345678912F43zhangjiangsoftwarepark               156yk�r��Z20000000000000000270000-000030000000000001100507601000000000000090030    CUP454  CUPAM0010010110000000006�Ĵ���011138111111110800040000043516S22000004500040000   00000000000000000002C894B5D\n" +
                "[9:50:15:810407][0515 0515]\n";
//        String strs1 ="0582.\u0002058200040000   00010000   1\u0000\u0000\u000100000000\u0000000000200�>D����\u0018\u0000\u0000\u0000\u0000\u0010\u0000\u0000�16620058511117064100000000000001000005100941100171100941100510251204047011021000608000100050800010005369558990000000000001=0509567890123456099999558990000000000001=1561560000000000000003976999236000002070000000000000000000000=000000000000=00000000017110F4112345F42012345678912F43zhangjiangsoftwarepark               156yk�r��Z20000000000000000270000-000030000000000001100507601000000000000090030    CUP454  CUPAM0010010110000000006�Ĵ���011138111111110800040000043516S22000004500040000   00000000000000000002C894B5D\n" +
//                "[9:50:15:810407][0515 0515]\n" +
//                "0515.\u0002051500040000   00010000   1\u0000\u0000\u000100000000\u0000000000220�:D����\u0010\u0000\u0000\u0000@\u0010\u0000\u0000�16620058511117064120000000000001000005100943230171110943230510040470110200008000100050800010005369558990000000000001=0509567890123456099999558990000000000001=1561560000000000000003976999236000002070000000000000000000000=000000000000=00000000017111F4112345F42012345678912F43zhangjiangsoftwarepark               1560270000-00003000000000000110050200017110051009411000000010005000000100050800040000043516S30000035500040000   0000000000000000000899ADBDC\n" +
//                "[16:11:49:784579][0591 0591]\n";

        if (strs1.equals(strs)){
            System.out.println("ok");
        }


        byte[] resp2 = strs1.getBytes();
        String mm = bytes2Hex(resp2);
        if (resp.equals(resp2)){
            System.out.println("相等？");
        }
//
//        if (resp.equals(resp1)){
//            System.out.println("相等？");
//        }


//        String str = hexStr.trim().replace("\r\n", "").replace("\r","").replace("\n", "").replace(" ", "").substring(8);
//        String str = new String(resp2).trim().replace("\r\n", "").replace("\r","").replace("\n", "").replace(" ", "").substring(8);
        String str = new String(resp).trim().replace("\r\n", "").replace("\r","").replace("\n", "").replace(" ", "").substring(8);
//        if (hexStr.equals(str)){
//            System.out.println("相等");
//        }
        if (mm.equals(hexStr)){
            System.out.println("相等");
        }
//        String str = hexStr.trim().replace("\r\n", "").replace("\r","").replace("\n", "").replace(" ", "");
        //将报文解析成字节数组
        byte[] retByte = parseTo8583bytes(str);
        //解析8583报文体
        Map<String,String> fieldMap = parse8583(retByte);
        //获取有序的keys
        List<String> keyList = getKeyList(fieldMap);
        //输出各域信息
        for(String key:keyList){
            System.out.println("["+key+"] = "+fieldMap.get(key));
        }
    }

    public static String bytes2Hex(byte[] src) {
        if (src == null || src.length <= 0) {
            return null;
        }

        char[] res = new char[src.length * 2]; // 每个byte对应两个字符
        final char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        for (int i = 0, j = 0; i < src.length; i++) {
            res[j++] = hexDigits[src[i] >> 4 & 0x0f]; // 先存byte的高4位
            res[j++] = hexDigits[src[i] & 0x0f]; // 再存byte的低4位
        }

        return new String(res);
    }

    public static byte[] hex2byte(String hexStr) {
        if (hexStr == null || hexStr.equals("")) {
            return null;
        }

        hexStr = hexStr.toUpperCase();
        int length = hexStr.length() / 2;
        char[] hexChars = hexStr.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) ( charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]) );
        }
        return d;
    }


    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


    // 报文处理函数
    private static Map<String, String> parse8583(byte[] byteArr)
            throws UnsupportedEncodingException {
        Map<String,String> fieldMap = new HashMap<String,String>();
        //获取报文头信息
        parseHeaders(byteArr, fieldMap);
        // 获取MsgType
        parseMsgType(byteArr, fieldMap);
        // 获取位图
        String bitMap1_value = getBitMap(byteArr);
        fieldMap.put("F1", bitMap1_value);
        // 根据位图判断存在哪些域及获取域的值
        parseFields(byteArr, fieldMap, bitMap1_value);
        // 返回
        return fieldMap;
    }
    //获取报文头信息
    private static void parseHeaders(byte[] byteArr,Map<String, String> fieldMap)
            throws UnsupportedEncodingException {
        for(int i=1;i<=10;i++){
            Properties headProperties = map.get("H"+i);
            if(headProperties == null) continue;

            int head_length = Integer.parseInt(headProperties.getProperty("length"));
            byte[] head_value_byte = new byte[head_length];
            System.arraycopy(byteArr, currentIndex, head_value_byte, 0, head_length);
            currentIndex += head_length;

            String isBCD = headProperties.getProperty("isBCD");
            if("true".equals(isBCD)){
                String head_value = bcd_bytes_to_String(head_value_byte);
                fieldMap.put(headProperties.getProperty("name"), head_value);
            }else{
                String head_value = new String(head_value_byte, headProperties.getProperty("encoding"));
                fieldMap.put(headProperties.getProperty("name"), head_value);
            }
        }
    }
    //bcd_bytes_to_String
    private static String bcd_bytes_to_String(byte[] bytes){
        StringBuffer sb = new StringBuffer(bytes.length*2);
        for(int i=0;i<bytes.length;i++){
            sb.append(String.valueOf((int)bytes[i]));
        }
        return sb.toString();
    }
    //解析各个域
    private static void parseFields(byte[] byteArr,Map<String, String> fieldMap,String bitMap1_value_2) throws UnsupportedEncodingException {
        List<Integer> exitFieldList = getExitField(bitMap1_value_2);
        // 获取各域值
        for(int i=0;i<exitFieldList.size();i++){
            int field = exitFieldList.get(i);
            System.out.println("解析域Field="+field);
            Properties fieldProperties = map.get("F"+field);
            //如果不存在，跳过
            if(fieldProperties == null)continue;

            String field_variable_flag = fieldProperties.getProperty("variable_flag");
            if(field_variable_flag == null || "".equals(field_variable_flag)){
                int field_length = Integer.parseInt(fieldProperties.getProperty("length"));
                byte[] field_value_byte = new byte[field_length];
                System.arraycopy(byteArr, currentIndex, field_value_byte, 0, field_length);
                currentIndex += field_length;
                String field_value = new String(field_value_byte, fieldProperties.getProperty("encoding"));
                fieldMap.put(fieldProperties.getProperty("name"), field_value);
            }else{
                //先获取变长域的长度值
                int variable_flag_length = Integer.parseInt(field_variable_flag);
                byte[] variable_flag_byte = new byte[variable_flag_length];
                System.arraycopy(byteArr, currentIndex, variable_flag_byte, 0, variable_flag_length);
                currentIndex += variable_flag_length;
                String variable_flag_value = new String(variable_flag_byte, fieldProperties.getProperty("encoding"));
                //再获取变长域的真实值
                int field_length = Integer.parseInt(variable_flag_value);
                byte[] field_value_byte = new byte[field_length];
                System.arraycopy(byteArr, currentIndex, field_value_byte, 0, field_length);
                currentIndex += field_length;
                String field_value = new String(field_value_byte, fieldProperties.getProperty("encoding"));
                fieldMap.put(fieldProperties.getProperty("name"), field_value);
            }
        }
    }
    //获取二进制位图字符串
    private static String getBitMap(byte[] byteArr) {
        Properties bitMap1 = map.get("bitmap1");
        int bitMap1_length = Integer.parseInt(bitMap1.getProperty("length"));
        byte[] bitMap1_value_byte = new byte[bitMap1_length];
        System.arraycopy(byteArr, currentIndex, bitMap1_value_byte, 0,bitMap1_length);
        currentIndex += bitMap1_length;
        String bitMap1_value_2 = binary(bitMap1_value_byte, 2);
        if (bitMap1_value_2.startsWith("1")) {
            Properties bitMap2 = map.get("bitmap2");
            int bitMap2_length = Integer.parseInt(bitMap2.getProperty("length"));
            byte[] bitMap2_value_byte = new byte[bitMap2_length];
            System.arraycopy(byteArr, currentIndex, bitMap2_value_byte, 0,bitMap2_length);
            currentIndex += bitMap2_length;
            String bitMap2_value_2 = binary(bitMap2_value_byte, 2);
            bitMap1_value_2 += bitMap2_value_2;
        }
        return bitMap1_value_2;
    }
    //解析MsgType
    private static void parseMsgType(byte[] byteArr,Map<String, String> mapRet)
            throws UnsupportedEncodingException {
        Properties msgType = map.get("MsgType");
        int msgType_length = Integer.parseInt(msgType.getProperty("length"));
        byte[] msgType_value_byte = new byte[msgType_length];
        System.arraycopy(byteArr, currentIndex, msgType_value_byte, 0,msgType_length);
        currentIndex += msgType_length;
        String msgType_value = new String(msgType_value_byte, msgType.getProperty("encoding"));
        mapRet.put("F0", msgType_value);
    }
    //根据二进制位图字符串获取存在的域
    private static List<Integer> getExitField(String bitMap2String) {
        List<Integer> exitFieldList = new ArrayList<Integer>();
        for (int i=2;i<=bitMap2String.length();i++) {
            int field_index = Integer.parseInt(String.valueOf(bitMap2String.charAt(i-1)));
            if (field_index == 1) {
                exitFieldList.add(i);
            }
        }
        return exitFieldList;
    }
    //将报文解析成字节数组
    private static byte[] parseTo8583bytes(String str) {
        String value = null;
        byte[] retByte = new byte[str.length() / 2];
        for (int i = 0; i < str.length(); i = i + 2) {
            value = str.substring(i, i + 2);
            retByte[i / 2] = (byte) Integer.parseInt(value, 16);
        }
        return retByte;
    }
    //获取有序的keys
    private static List<String> getKeyList(Map<String, String> fieldMap) {
        Set<String> keySet = fieldMap.keySet();
        List<String> keyList = new ArrayList<String>(keySet);
        Collections.sort(keyList, new Comparator<String>(){
            @Override
            public int compare(String str1, String str2) {
                // TODO Auto-generated method stub
                Integer value1 = Integer.parseInt(str1.substring(1));
                Integer value2 = Integer.parseInt(str2.substring(1));
                return value1.compareTo(value2);
            }});
        Collections.sort(keyList, new Comparator<String>(){
            @Override
            public int compare(String str1, String str2) {
                // TODO Auto-generated method stub
                char value1 = str1.charAt(0);
                char value2 = str2.charAt(0);
                return value1 < value2 ? 1 : 0;
            }});
        return keyList;
    }
    //将字节数组转换成二进制字符串
    private static String binary(byte[] bytes, int radix) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(byteToBit(b));
        }
        return sb.toString();
    }
    /**
     * Byte转Bit
     */
    private static String byteToBit(byte b) {
        return "" + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1)
                + (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1)
                + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1)
                + (byte) ((b >> 1) & 0x1) + (byte) ((b >> 0) & 0x1);
    }


}