package the.trav.statemachine

import groovy.util.logging.Log4j
import org.apache.commons.lang.StringUtils

@Log4j
class StateMachine {
   private Map stateMachineMap = [:]
   private def currentState, currentEvent

    static def transition(event, state_machine, subject) {
        def transition = state_machine[subject.state].find { it instanceof Map && it.event == event && (!it.guard || it.guard(subject)) }
        if (transition) {
            if (transition.action) {
                transition.action(subject)
            }
            subject.state = transition.to
            def newStateEntry = state_machine[subject.state]
            if (newStateEntry && newStateEntry.first() instanceof Closure) {
                newStateEntry.first().call(subject)
            }
        } else {
            log.warn("For type=${subject.class} id=${subject.id} state='${subject.state}' the event='$event' does not apply")
        }
    }

    static Map build(Closure closure) {
        def stateMachine = new StateMachine()

        closure.delegate = stateMachine
        closure.call()

        stateMachine.stateMachineMap
    }

    void state(def stateKey, Closure closure = null) {
        if (!stateMachineMap.containsKey(stateKey)) {
            stateMachineMap[stateKey] = []
        }
        currentState = stateKey
        if (closure) {
            closure.delegate = this
            closure.call()
        }
    }

    void event(Map options = [:], Object... args) {
        def eventName = args[0]
        Closure closure = null
        if (args[-1] instanceof Closure) {
            closure = args[-1]
        }
        currentEvent = [event: eventName, to: options.to ?: currentState]
        if (options.action) {
            currentEvent.action = options.action
        }
        if (options.guard) {
            currentEvent.guard = options.guard
        }

        if (closure) {
            closure.delegate = this
            closure.call()
        }

        stateMachineMap[currentState] << currentEvent
    }

    void transitionTo(def stateKey) {
        currentEvent['to'] = stateKey
    }

    def to = this.&transitionTo

    void action(Closure actionClosure) {
        currentEvent['action'] = actionClosure
    }

    void guard(Closure actionClosure) {
        currentEvent['guard'] = actionClosure
    }

    void onEntry(Closure entryAction) {
        List newStateEntry = stateMachineMap[currentState]
        if (newStateEntry && newStateEntry.first() instanceof Closure) {
            newStateEntry[0] = entryAction
        } else {
            newStateEntry.add(0, entryAction)
        }
    }

    def methodMissing(String name, args) {
        if (name.startsWith('on')) {
            switch (args.length) {
                case 1:
                    if (args[0] instanceof Map) {
                        event(StringUtils.uncapitalise(name[2..-1]), *:args[0])
                    } else {
                        event(StringUtils.uncapitalise(name[2..-1]), args[0])
                    }
                    break
                case 2:
                    event(StringUtils.uncapitalise(name[2..-1]), *:args[0], args[1])
                    break
                default:
                    event(StringUtils.uncapitalise(name[2..-1]))
            }
        } else {
            throw new MissingMethodException(name, this.class, args)
        }
    }
}

