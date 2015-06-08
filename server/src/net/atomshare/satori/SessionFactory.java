package net.atomshare.satori;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class SessionFactory {
    private static final String host = "satori.tcs.uj.edu.pl";
    private static final int thriftPort = 2889;
    private static final int blobsPort = 2887;
    private static final int connectionCount = 4;

	private final Map<String, Session> cached = new HashMap<>();
	private final BlockingQueue<TProtocol> connectionPool = new LinkedBlockingQueue<>();
	private final BlockingQueue<Unit> connectionsToCreate = new LinkedBlockingQueue<>();
	private final ForkJoinPool forkJoinPool = new ForkJoinPool(connectionCount * 3);

    public SessionFactory() throws IOException, TTransportException {
        for(int i=0; i < connectionCount; i++) {
            try {
                connectionsToCreate.put(Unit.VALUE);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void startConnectionDestroyer() {
        // Now I am become Death, the destroyer of worlds.
        new Thread(() -> {
            try {
                //noinspection InfiniteLoopStatement
                while (true) {
                    // Replace all connections every `interval` seconds
                    final long interval = 120;
                    final long delay = interval * 1000 / connectionCount;
                    Thread.sleep(delay);
                    try {
                        recreateConnection();
                    } catch (TException e) {
                        e.printStackTrace();
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void recreateConnection() throws TException {
        Connection conn = takeConnection();
        conn.destroy();
    }

    public void startConnectionCreator() {
        new Thread(() -> {
            try {
                //noinspection InfiniteLoopStatement
                while (true) {
                    Unit token = connectionsToCreate.take();
                    while (true) {
                        try {
                            addConnection();
                            break;
                        } catch(IOException|TTransportException ex) {
                            ex.printStackTrace();
                            Thread.sleep(2000);
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void addConnection() throws IOException, TTransportException {
        connectionPool.add(createProtocol());
        System.err.println("Connection created.");
    }

    public Connection takeConnection() throws TException {
		System.err.println("takeConnection " + connectionStat());
        TProtocol protocol;
        try {
            protocol = connectionPool.take();
        } catch(InterruptedException ex) {
            throw new TException(ex);
        }
        return new SessionConnection(protocol);
    }

	private String connectionStat() {
		return connectionPool.size() + "/" + connectionCount;
	}

	public interface Producer<T> {
        T produce(Connection conn) throws TException;
    }

    public <T> T withConnection(Producer<T> producer) {
		return wrapInPool(() -> {
			while (true) {
				Connection conn = takeConnection();
				try {
					return producer.produce(conn);
				} catch (TTransportException ex) {
					ex.printStackTrace();
					conn.destroy();
					conn = null;
				} finally {
					if (conn != null)
						conn.close();
				}
			}
		});
    }

	@SuppressWarnings("unchecked")
	private <S, E extends Exception> S wrapInPool(ThrowingCallable<S, E> callable) {
		// Makes parallelStream() use our own pool.
		// See: http://stackoverflow.com/questions/21163108/custom-thread-pool-in-java-8-parallel-stream
		try {
			return forkJoinPool.submit(() -> {
				try {
					return callable.call();
				} catch(Exception e) {
					throw new RuntimeException(e); // TODO
				}
			}).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private interface ThrowingCallable<S, E extends Exception> {
		S call() throws E;
	}

    private class SessionConnection
            extends Connection {
        public SessionConnection(TProtocol protocol) {
            super(protocol);
        }

        @Override
        public void destroy()  {
            synchronized (SessionFactory.this) {
                try {
                    connectionsToCreate.put(Unit.VALUE);
                } catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
            }
            protocol.getTransport().close();
        }

        @Override
        public void close() {
            synchronized (SessionFactory.this) {
                try {
                    connectionPool.put(protocol);
					System.err.println("closeConnection " + connectionStat());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private Socket createSecureSocket() throws IOException {
        SSLSocket socket = (SSLSocket)
                SSLSocketFactory.getDefault().createSocket(host, thriftPort);
        socket.setEnabledProtocols(new String[]{"TLSv1"});
        return socket;
    }

    private TProtocol createProtocol() throws IOException, TTransportException {
        Socket socket = createSecureSocket();
        socket.setSoTimeout(10000);
        return new TBinaryProtocol(new TFramedTransport(new TSocket(socket)));
    }

    public Session get(String cred) throws TException {
		Session sess;
		synchronized (cached) {
			sess = cached.get(cred);
		}
		if(sess == null) {
			synchronized (cached) {
				cached.put(cred, sess = new Session(this, cred));
			}
            sess.init();
        }
        return sess;
    }
}
