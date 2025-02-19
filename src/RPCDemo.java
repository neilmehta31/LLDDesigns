// First, let's create a simple RPC server

import java.io.*;
import java.net.*;

// Interface defining the remote procedures
interface CalculatorService {
    int add(int a, int b);
    int subtract(int a, int b);
    int multiply(int a, int b);
}

// Server implementation of the calculator service
class CalculatorServiceImpl implements CalculatorService {
    @Override
    public int add(int a, int b) {
        return a + b;
    }

    @Override
    public int subtract(int a, int b) {
        return a - b;
    }

    @Override
    public int multiply(int a, int b) {
        return a * b;
    }
}

// Server class to handle RPC requests
class RPCServer {
    private ServerSocket serverSocket;
    private CalculatorService calculator;

    public RPCServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        calculator = new CalculatorServiceImpl();
        System.out.println("RPC Server started on port " + port);
    }

    public void start() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket, calculator)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Handler for each client connection
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private CalculatorService calculator;

        public ClientHandler(Socket socket, CalculatorService calculator) {
            this.clientSocket = socket;
            this.calculator = calculator;
        }

        @Override
        public void run() {
            try (
                    ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())
            ) {
                while (true) {
                    // Read the RPC request
                    RPCRequest request = (RPCRequest) in.readObject();

                    // Process the request
                    int result = 0;
                    switch (request.getMethod()) {
                        case "add":
                            result = calculator.add(request.getParams()[0], request.getParams()[1]);
                            break;
                        case "subtract":
                            result = calculator.subtract(request.getParams()[0], request.getParams()[1]);
                            break;
                        case "multiply":
                            result = calculator.multiply(request.getParams()[0], request.getParams()[1]);
                            break;
                    }

                    // Send the response
                    out.writeObject(new RPCResponse(result));
                }
            } catch (EOFException e) {
                // Client disconnected
                System.out.println("Client disconnected");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

// RPC Client implementation
class RPCClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public RPCClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public int callMethod(String methodName, int... params) throws IOException, ClassNotFoundException {
        // Create and send RPC request
        RPCRequest request = new RPCRequest(methodName, params);
        out.writeObject(request);

        // Receive and return response
        RPCResponse response = (RPCResponse) in.readObject();
        return response.getResult();
    }

    public void close() throws IOException {
        socket.close();
    }
}

// Request and Response classes for RPC communication
class RPCRequest implements Serializable {
    private String method;
    private int[] params;

    public RPCRequest(String method, int[] params) {
        this.method = method;
        this.params = params;
    }

    public String getMethod() { return method; }
    public int[] getParams() { return params; }
}

class RPCResponse implements Serializable {
    private int result;

    public RPCResponse(int result) {
        this.result = result;
    }

    public int getResult() { return result; }
}

// Demo class to show RPC in action
public class RPCDemo {
    public static void main(String[] args) {
        // Start server in a separate thread
        new Thread(() -> {
            try {
                RPCServer server = new RPCServer(5000);
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Give server time to start
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Client code
        try {
            RPCClient client = new RPCClient("localhost", 5000);

            // Make RPC calls
            System.out.println("Adding 5 + 3: " + client.callMethod("add", 5, 3));
            System.out.println("Subtracting 10 - 4: " + client.callMethod("subtract", 10, 4));
            System.out.println("Multiplying 6 * 7: " + client.callMethod("multiply", 6, 7));

            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}