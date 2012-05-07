package the.trav.statemachine

import org.junit.Test


class StateMachineTest {

   @Test
   void testBasicTransition() {
      def subject = [state:"state_a"]
      def event = "event_a"
      def machineMap = [
         "state_a" : [[event:"event_a", to:"state_b"]],
         "state_b" : []
      ]

      StateMachine.transition(event, machineMap, subject)

      assert "state_b" == subject.state
   }

   @Test
   void testGuardedTransition() {
      def subject = [state:"state_a"]
      def event = "event_a"
      def machineMap = [
         "state_a" : [[guard:{false}, event:"event_a", to:"state_b"],
                      [guard:{true}, event:"event_a", to:"state_c"]],
         "state_b" : [],
         "state_c" : []
      ]

      StateMachine.transition(event, machineMap, subject)

      assert "state_c" == subject.state
   }

   @Test
   void testActionPerformed() {

      def subject = [state:"state_a", actionPerformed:false]
      def event = "event_a"
      def machineMap = [
         "state_a" : [[event:"event_a", to:"state_b", action:{it.actionPerformed = true}]],
         "state_b" : []
      ]

      StateMachine.transition(event, machineMap, subject)

      assert "state_b" == subject.state
      assert subject.actionPerformed
   }
}
