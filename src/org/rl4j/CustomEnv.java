package org.rl4j;

import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.space.ActionSpace;
import org.deeplearning4j.rl4j.space.ObservationSpace;

public class CustomEnv<O, A, AS extends ActionSpace<A>> implements MDP<O, A, AS> {
    @Override
    public ObservationSpace<O> getObservationSpace() {
        return null;
    }

    @Override
    public AS getActionSpace() {
        return null;
    }

    @Override
    public O reset() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public StepReply<O> step(A a) {
        return null;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public MDP<O, A, AS> newInstance() {
        return null;
    }
}
