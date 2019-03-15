package db;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;

class jdbcInterface {
    private static PrintWriter stdErr = new PrintWriter(System.err, true);
    private static PrintWriter stdOut = new PrintWriter(System.out, true);
    private static BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Reads and checks given String then passes parameters return query object
     *
     * @throws IOException io
     */
    void produceCrackerReport() throws IOException {

        AtomicReference<String> input = new AtomicReference<String>();
        PreparedStatement crackerReport, hatJoin;
        DecimalFormat format = new DecimalFormat("##.00");
        do {
            stdErr.print("cid>");
            stdErr.flush();
            try {
                Scanner sc = new Scanner(System.in);
                input.set(sc.nextLine());
                try {
                    DatabaseApplication.christmas.getConn().setAutoCommit(false);
                    try {
                        crackerReport = DatabaseApplication.christmas.getConn().prepareStatement("SELECT cid, name, quantity, g.description AS giftdesc, g.price, royalty, joke, salesprice FROM cracker " +
                                "NATURAL JOIN gift g NATURAL JOIN joke  WHERE cid=?");
                        hatJoin = DatabaseApplication.christmas.getConn().prepareStatement("SELECT * FROM cracker NATURAL JOIN hat WHERE  cid = ?");
                        //parsing here as It would not compile with string
                        crackerReport.setInt(1, Integer.valueOf(input.get()));
                        hatJoin.setInt(1, Integer.valueOf(input.get()));
                        //crackerReport.setString(1,input.get());
                        //operator does not exist: integer = character varying
                        //crackerReport.executeUpdate();
                        ResultSet resultSet = crackerReport.executeQuery();
                        ResultSet rs = hatJoin.executeQuery();
                        while (resultSet.next()) {
                            int id = resultSet.getInt("cid");
                            String name = resultSet.getString("name");
                            //int gid = resultSet.getInt("gid");
                            //int hid = resultSet.getInt("hid");
                            int salesprice = resultSet.getInt("salesprice");
                            int quantity = resultSet.getInt("quantity");
                            //gift
                            String giftdescription = resultSet.getString("giftdesc");
                            Double giftprice = resultSet.getDouble("price");

                            //joke
                            String joke = resultSet.getString("joke");
                            Double royalty = resultSet.getDouble("royalty");

                            stdErr.println("cracker id: " + id);
                            stdErr.println("name: " + name);
                            stdErr.println("description of the gift: " + giftdescription);
                            stdErr.println("joke: " + joke);
                            double unitcost = 0;
                            while (rs.next()) {
                                String hatdescrtiption = rs.getString("description");
                                stdErr.println("description of the hat: " + hatdescrtiption);
                                stdErr.println("unit saleprice: " + salesprice );
                                Double hatprice = rs.getDouble("price");
                                unitcost = giftprice + royalty + hatprice;
                                stdErr.println("unit cost price: " + unitcost);
                            }
                            stdErr.println("quantity sold: " + quantity);
                            stdErr.println("The net profit: " + format.format((double)quantity*(salesprice-unitcost)));
                            stdErr.flush();
                        }
                        DatabaseApplication.christmas.getConn().commit();
                        break;
                    } catch (Exception e) {
                        e.getLocalizedMessage();
                        stdErr.print(" Connection rolled back\n");
                        DatabaseApplication.christmas.getConn().rollback();
                    }
                } catch (SQLException e) {
                    stdErr.println("Invalid input");
                    stdErr.print(e.getLocalizedMessage());
                    stdErr.println(" Please try again");
                    stdErr.flush();
                }
            } catch (NumberFormatException nfe) {
                nfe.getLocalizedMessage();
            }
        } while (true);
    }

    void produceJokeReport() throws IOException{
        AtomicReference<String> input = new AtomicReference<String>();
        PreparedStatement jokeReport;
        DecimalFormat format = new DecimalFormat("##.00");
        do {
            stdErr.print("jid>");
            stdErr.flush();
            try {
                Scanner sc = new Scanner(System.in);
                input.set(sc.nextLine());
                try {
                    jokeReport = DatabaseApplication.christmas.getConn().prepareStatement("SELECT DISTINCT jid, joke, royalty, SUM(COUNT(quantity)*cracker.quantity)  OVER (ORDER BY jid) FROM joke NATURAL JOIN cracker WHERE jid = ? GROUP BY jid,cracker.quantity");
                    jokeReport.setInt(1, Integer.valueOf(input.get()));
                    ResultSet rs = jokeReport.executeQuery();
                    while (rs.next()) {
                        int jid = rs.getInt("jid");
                        String joke = rs.getString("joke");
                        Double royalty = rs.getDouble("royalty");
                        int sum = rs.getInt("sum");

                        stdErr.println("Joke's id: " + jid);
                        stdErr.println("joke: " + joke);
                        stdErr.println("Unit royalty payment: " + royalty);
                        stdErr.println("The number of times the joke was used: " + sum);
                        stdErr.println("Total royalty payment: " + format.format((double) sum* royalty));
                        stdErr.flush();
                    }
                    break;
                } catch (SQLException e) {
                    stdErr.println("Invalid input");
                    stdErr.print(e.getLocalizedMessage());
                    stdErr.println(" Please try again");
                    stdErr.flush();
                }

            } catch (NumberFormatException nfe) {
                nfe.getLocalizedMessage();
            }
        } while (true);

    }

    void insertNewCracker() throws IOException {
        String INSERT_NEW = "INSERT INTO cracker VALUES (?,?,?,?,?,?,?)";
        final String DELIM = "_";
        //detected by the database and handled by java code
        String cid,name,jid,gid,hid,salesprice,quantity;
        do {
            try {
                stdErr.print("cracker [cid_name_jid_gid_hid_salesprice]>");
                stdErr.flush();
                StringTokenizer tokenizer = new StringTokenizer(stdIn.readLine(), DELIM);
                if (tokenizer.countTokens() != 6) {
                    stdErr.println("Invalid input(need 6 values)");
                } else {
                    try {
                        cid = tokenizer.nextToken();
                        name = tokenizer.nextToken();
                        jid = tokenizer.nextToken();
                        gid = tokenizer.nextToken();
                        hid = tokenizer.nextToken();
                        salesprice = tokenizer.nextToken();
                        quantity = "0";

                        PreparedStatement preparedStatement = DatabaseApplication.christmas.getConn().
                                prepareStatement(INSERT_NEW);
                        preparedStatement.setInt(1, Integer.parseInt(cid));
                        preparedStatement.setString(2, name);
                        preparedStatement.setInt(3, Integer.parseInt(jid));
                        preparedStatement.setInt(4, Integer.parseInt(gid));
                        preparedStatement.setInt(5, Integer.parseInt(hid));
                        preparedStatement.setDouble(6, Double.parseDouble(salesprice));
                        preparedStatement.setInt(7, Integer.parseInt(quantity));
                        preparedStatement.execute();
                        stdErr.println("Successfully inserted: cid = " + cid +", cracker name = "+ name
                        + ", jid = "+ jid + ", gid = "+ gid + ", hid = " + hid + ", salesplice = " +
                        salesprice + ", quantity = " + quantity);
                        stdErr.flush();
                        break;
                    } catch (SQLException e) {
                        stdErr.println("Invalid input");
                        stdErr.print(e.getLocalizedMessage());
                        stdErr.println(" Please try again");
                        stdErr.flush();
                    }
                }
            } catch (NumberFormatException nfe){
                nfe.getLocalizedMessage();
            }
        } while (true);
    }
}
