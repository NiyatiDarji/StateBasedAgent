# StateBasedAgent
Implementation of state based agent in krislet environment

My state-based agent performs the actions based on the following rules:
1. Agent has 2 internal states: kick_bit =0 and kick_bit =1. Agent can kick alternatively i.e. agent will kick 1st time, will dash 2nd time, kick 3rd time, dash 4th time and so on
2. If you don't know where ball is then turn right and wait for new info. No change in kick_bit
3. If ball is too far to kick it and kick_bit = 1 then turnordash () i.e. based on direction either perform dash or turn. If direction is correct then dash else turn. No change in the kick_bit.
4. If ball is too far to kick it and kick_bit = 0 then turnordash () i.e. based on direction either perform dash or waitturn. If direction is correct then dash else turn right and wait for new info. No change in the kick_bit.
5. If ball is at a distance less than 1 and if the kick_bit = 1, then kick the ball . Change kick_bit to 0.
6. If ball is at a distance less than 1 and if the kick_bit = 0, then dash. Change kick_bit to 1.	
My state-based agent performs the actions based on the following FSM:
 
To implement these rules, I have used 2bit binary combination for environment variable 
1st bit: B - set if ball object != null
2nd bit: D - set if distance > 1.0
combination = B + D 
All the environment variables combination with kick_bit internal state has a corresponding action and the next kick_bit internal state .This is stored in “question2.txt” file.
“question2.txt” format- combination: kick_bit - action (action_argument): next_Kick_bit
action_argument can be one or two arguments separated by comas.
e.g.: ‘10:1-kick(100,direction):0’ means B = 1, D =0 i.e. We got the ball object, its distance >1 is false, kick_bit is 1. Action to be performed is kick with power 100 and in the ball direction. Kick_bit  for next cycle is set to 0.
To edit the behaviour of the agent, one can change the actions. Actions should be one among kick, justdash, turnordash and waitturn.
My Brain.java file run(): First gets the environment variables (ball B, distance D), checks their status and sets their bits accordingly. This forms the entire combination= B+D. The kick_bit already has a value.
action = findRuleGetAction(combination, kick_bit): reads the question2.txt file, fetch the line for the combination plus kick_bit and returns the corresponding action which includes the next kick_bit aswell. Now, the run() will perform this action with its arguments.
