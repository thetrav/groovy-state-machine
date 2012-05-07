package the.trav.statemachine

class StateMachine {

   static def transition(event, state_machine, subject) {
      def transition = state_machine[subject.state].find { it.event == event && (!it.guard || it.guard(subject)) }
      if (transition) {
         if (transition.action) {
            transition.action(subject)
         }
         subject.state = transition.to
      }
   }
}
