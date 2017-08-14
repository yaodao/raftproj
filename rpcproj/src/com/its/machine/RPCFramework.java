package com.its.machine;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * RpcFramework
 */

class ProcessTask implements Runnable {

    public ProcessTask(Socket socket, Object service) {
        this.socket = socket;
        this.service = service;
    }

    private Socket socket;
    private Object service;

    @Override
    public void run() {
        try {
            try {
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                try {
                    //service是服务器端提供服务的对象，但是，要通过获取到的调用方法的名称，参数类型，以及参数来选择对象的方法，并调用。获得方法的名称
                    String methodName = input.readUTF();
                    Class<?>[] parameterTypes = (Class<?>[]) input.readObject();//获得参数的类型
                    Object[] arguments = (Object[]) input.readObject();//获得参数
                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                    try {
                        Method method = service.getClass().getMethod(methodName, parameterTypes);//通过反射机制获得方法
                        Object result = method.invoke(service, arguments);//通过反射机制获得类的方法，并调用这个方法
                        output.writeObject(result);//将结果发送
                    } catch (Throwable t) {
                        output.writeObject(t);
                    } finally {
                        output.close();
                    }
                } finally {
                    input.close();
                }
            } finally {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class RPCFramework {

    /**
     * 提供服务
     *
     * @param service 服务实现
     * @param port    服务端口
     * @throws Exception
     */
    public static void exportService(Object service, int port) throws IOException {
        if (service == null) {
            throw new IllegalArgumentException("service instance == null");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port " + port);
        }
        System.out.println("Export service " + service.getClass().getName() + " on port " + port);
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            boolean flag = true;
            while(flag) {
                try {
                    Socket socket = server.accept();
                    //服务器端一旦收到消息，就创建一个线程进行处理
                    new Thread(new ProcessTask(socket, service)).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.close();
        }

    }



    /**
     * 引用服务
     *
     * @param <T>            接口泛型
     * @param interfaceClass 接口类型
     * @param host           服务器主机名
     * @param port           服务器端口
     * @return 远程服务
     * @throws Exception 原理是,获得服务器端接口的一个“代理”的对象. 对这个对象的所有操作都会调用invoke函数，
     *                   在invoke函数中，是将被调用的函数名，参数列表和参数发送到服务器，并接收服务器处理的结果.
     */
    public static <T> T getService(final Class<T> interfaceClass, final String host, final int port) throws Exception {
        if (interfaceClass == null) {
            throw new IllegalArgumentException("Interface class == null");
        }
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("The " + interfaceClass.getName() + " must be interface class!");
        }
        if (host == null || host.length() == 0) {
            throw new IllegalArgumentException("Host == null!");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port " + port);
        }

        System.out.println("Get remote service " + interfaceClass.getName() + " from server " + host + ":" + port);
        return getProxy(interfaceClass, host, port);
    }

    private static <T> T getProxy(Class<T> interfaceClass, final String host, final int port) {

        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Socket socket = new Socket(host, port);
                try {
                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                    try {
                        //将要调用的函数写给服务端
                        output.writeUTF(method.getName());
                        output.writeObject(method.getParameterTypes());
                        output.writeObject(args);
                        //得到服务端的响应
                        ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                        try {
                            Object result = input.readObject();
                            if (result instanceof Throwable) {
                                throw (Throwable) result;
                            }
                            return result;
                        } finally {
                            input.close();
                        }
                    } finally {
                        output.close();
                    }
                } finally {
                    socket.close();
                }
            }
        };
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, handler);
    }
}
