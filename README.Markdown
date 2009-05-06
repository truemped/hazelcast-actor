# What

This project is a simple library implementing the actor model for concurrant programming. Using Hazelcast for the
message passing allows a very simple scaling of the actors accross multiple nodes.

# How

## A simple echo actor

    final class EchoActor extends AbstractActor<String, String> {

        public EchoActor() {
            super(new DefaultNamingStrategy(), EchoActor.class, true);
        }

        @Override public String call(String input)
        {
            return input;
        }
    }

## The actor worker

Occasionally you need to scale your actor, i.e. run the same actor with multiple threads. For this you can use the `ActorWorker`
and a corresponding factory to run the actor inside an `ExecutorService`.

    private class EchoActor extends AbstractActorWorker<String, String> {

        protected EchoActor(InputMessage<String> inputMsg, ITopic<OutputMessage<String>> topic) {
            super(inputMsg, topic);
        }

        @Override
        public String call(String input) {
            return input;
        }

    }

    class EchoActorFactory implements ActorWorkerFactory<String, String> {

        @Override
        public Class<? extends AbstractActorWorker<String, String>> getClazz() {
            return EchoActor.class;
        }

        @Override
        public AbstractActorWorker<String, String> newInstance(InputMessage<String> input, ITopic<OutputMessage<String>> topic) {
            return new EchoActor(input, topic);
        }

    }

In order to instantiate the actor you could then do something like this:

    ActorManager<String, String> echoManager = new ActorManager<String, String>(new DefaultNamingStrategy(), new EchoActorFactory());
    Thread manager = new Thread(echoManager);
    manager.start();


## And directors

    DirectorImpl<String, String> echoProxy = new DirectorImpl<String, String>(new DefaultNamingStrategy(), EchoActor.class);
    Future<String> future = echoProxy.call("Test");

Or you can call the same actor with a list of strings and you will get a list of futures (wow, multiple futures...)

    List<Future<String>> futures;
    List<String> strings = new ArrayList<String>() {
        private static final long serialVersionUID = 7328631044879958719L;
        {
            this.add("Test1");
            this.add("Test2");
        }
    };

    futures = echoProxy.call(strings);

# Notes

Actually I am not really sure if it is appropriate to run the actor within an `ExecutorService` but this is the only way
I could think of to allow recursive calls, e.g. Maybe someone else will guide me to the right path!?

