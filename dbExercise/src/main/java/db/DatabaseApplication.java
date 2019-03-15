package db;

import java.io.*;
import java.sql.SQLException;
import java.sql.Statement;


import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.postgresql.util.PSQLException;

public class DatabaseApplication {

	private static BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
	private static PrintWriter stdErr = new PrintWriter(System.err, true);
	static DBConn christmas = new DBConn();
	public static void main(String[] args) throws IOException {
		DatabaseApplication application = new DatabaseApplication();
		application.run();
	}

	/**
	 * Creates and runs Database application
	 *
	 * @throws IOException thorws exception
	 */

	private void run() throws IOException {

		populate();

		jdbcInterface jdbcInterface = new jdbcInterface();
		int choice = getChoice();

		while (choice != 0) {

			if (choice == 1) {
				jdbcInterface.produceCrackerReport();
			} else if (choice == 2) {
				jdbcInterface.produceJokeReport();
			} else if (choice == 3) {
				jdbcInterface.insertNewCracker();
			}

			choice = getChoice();
		}
		// Now, just tidy up by closing connection
		try {
			christmas.getConn().close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		stdErr.println("Connection closed!");
		stdErr.flush();
		
	}

	private void populate() {
		
		try {

			Statement statement = christmas.getConn().createStatement();

			// statement.addBatch("CREATE SCHEMA IF NOT EXISTS public;");

			statement.addBatch("CREATE TABLE IF NOT EXISTS Joke(" 
					+ "jid 		integer 			PRIMARY KEY,"
					+ "joke 	varchar(255) 		NOT NULL UNIQUE,"
					+ "royalty 	numeric(8,2) 		CHECK(royalty > 0));");
			
			statement.addBatch("CREATE TABLE IF NOT EXISTS Gift(" 
					+ "gid 			integer 		PRIMARY KEY,"
					+ "description 	varchar(255) 	NOT NULL," 
					+ "price		numeric(8,2)	CHECK(price>0));");
			
			statement.addBatch("CREATE TABLE IF NOT EXISTS Hat(" 
					+ "hid 			integer 		PRIMARY KEY,"
					+ "description 	varchar(255) 	NOT NULL," 
					+ "price 		numeric(6,2) 	CHECK(price>0));");

			statement.addBatch("CREATE TABLE IF NOT EXISTS Cracker(" 
					+ "cid 			integer 		PRIMARY KEY,"
					+ "name 		varchar(255)	NOT NULL UNIQUE,"
					+ "jid 			integer 		references Joke(jid) ON DELETE RESTRICT,"
					+ "gid			integer 		references Gift(gid) ON DELETE RESTRICT,"
					+ "hid			integer			references Hat(hid) ON DELETE RESTRICT,"
					+ "salesprice	numeric(8,2)	check (salesprice > 0) NOT NULL UNIQUE,"
					+ "quantity 	numeric 		check (quantity >= 0));");

			statement.executeBatch();
			System.out.println("Tables created");
			statement.clearBatch();

			try {

				File file = new File("src/main/resources/giftdata.csv");
				// File file1 = new File();
				// File file2 = new File();
				/*
				 * File file1 = new File("."); for(String fileNames :
				 * file1.list()) System.out.println(fileNames);
				 */

				CopyManager cm = new CopyManager((BaseConnection) christmas.getConn());

				FileReader fr = new FileReader(file);
				cm.copyIn("COPY Gift FROM STDIN WITH DELIMITER ',' ", fr);

				fr.close();

				File file2 = new File("src/main/resources/hatdata.csv");
				FileReader fr2 = new FileReader(file2);

				cm.copyIn("COPY Hat FROM STDIN WITH DELIMITER ',' ", fr2);
				fr2.close();

				File file3 = new File("src/main/resources/jokedata.csv");
				FileReader fr3 = new FileReader(file3);

				cm.copyIn("COPY Joke FROM STDIN WITH DELIMITER ',' ", fr3);
				fr3.close();

				File file1 = new File("src/main/resources/crackerdata.csv");
				FileReader fr1 = new FileReader(file1);

				cm.copyIn("COPY Cracker FROM STDIN WITH DELIMITER ',' CSV HEADER", fr1);
				fr1.close();
			} catch (PSQLException PSQLE){
				System.out.println("Tables already populated");
				PSQLE.getLocalizedMessage();
			}
			 catch (IOException sqlE) {
				sqlE.printStackTrace();
				System.out.println("SQL code is broken");

			}

			/*
			 * debug select *from cracker order by cid desc limit 10; 
			 * DROP table Joke CASCADE; 
			 * DROP table hat CASCADE; 
			 * DROP table gift CASCADE;
			 * DROP table cracker CASCADE; 
			 * DROP SCHEMA public CASCADE;
			 */
			/*
			 * PreparedStatement studentQuery =
			 * christmas.getConn().prepareStatement(
			 * "COPY Child FROM 'University/dbExercise/src/main/resources/Children.csv' WITH DELIMITER ',' CSV HEADER;"
			 * "FROM STDIN" ); studentQuery.executeUpdate();
			 *  studentQuery.close();
			 */
			System.out.println("Tables populated");

			statement.close();

		} catch (SQLException sqlE) {
			sqlE.printStackTrace();
			System.out.println("SQL code is broken");
		}
		System.out.println("Success");
	}

	/**
	 * Gives options to the user to chose from Presents user with menu of
	 * options and processes the selection.
	 *
	 * @return chosen input in the application
	 * @throws IOException io exception
	 */
	private int getChoice() throws IOException {

		do {

			int input;

			try {
				stdErr.println();
				stdErr.print("[0]  Quit\n" 
						+ "[1]  Produce a report for a cracker\n" 
						+ "[2]  Produce a report for a joke\n"
						+ "[3]  Insert a new cracker into the data base\n" 
						+ "choice>");
				stdErr.flush();

				input = Integer.parseInt(stdIn.readLine());

				if (0 <= input && 3 >= input) {

					return input;

				} else {
					stdErr.println("Invalid choice:  " + input);
				}
			} catch (NumberFormatException nfe) {
				stdErr.println(nfe);
			}
		} while (true);
	}

}
