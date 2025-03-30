public class Main
{
	public static void main(String[] args)
	{

		if (args[0].equals("SERVER")) {
			Server server = new Server();
			server.start();
		} else if (args[0].equals("CLIENT")) {
			ClientWindow window = new ClientWindow();
		} else {
			System.out.println("Invalid argument");
		}
	}
}