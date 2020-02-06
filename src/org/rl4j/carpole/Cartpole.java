/*******************************************************************************
 * Copyright (c) 2015-2019 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package org.rl4j.carpole;

import org.deeplearning4j.rl4j.learning.sync.qlearning.QLearning;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteDense;
import org.deeplearning4j.rl4j.mdp.gym.GymEnv;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdDense;
import org.deeplearning4j.rl4j.policy.DQNPolicy;
import org.deeplearning4j.rl4j.space.Box;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.util.DataManager;
import org.nd4j.linalg.learning.config.Sgd;
import org.rl4j.CMDHelper;

import java.io.IOException;
import java.util.logging.Logger;


/**
 * @author rubenfiszel (ruben.fiszel@epfl.ch) on 8/11/16.
 * <p>
 * Main example for Cartpole DQN
 **/
public class Cartpole {

    public static QLearning.QLConfiguration CARTPOLE_QL =
            new QLearning.QLConfiguration(
                    123,                 // 随机数
                    200,        // 每次尝试多少次
                    1500000,         // 尝试的总次数
                    2500,     // 大脑记忆容量
                    32,             // 训练的批次大小
                    200,// 更新目标网络的频率
                    10,           // 从多少步开始更新
                    0.01,       // reward scaling
                    0.99,            // MP策略的gamma，奖励递减
                    1.0,         //td-error clipping
                    0.1f,         // 探索度下限
                    1000,     // 什么时候开始退火
                    true         //double DQN
            );

    public static DQNFactoryStdDense.Configuration CARTPOLE_NET =
            DQNFactoryStdDense.Configuration.builder()
                    .l2(0.001)
                    .updater(new Sgd(0.01))
                    .numHiddenNodes(32)
                    .numLayer(3).build();

    public static void main(String[] args) throws IOException {
        // 启动http服务
        CMDHelper.startHttpServer();
        cartPole();
        loadCartpole();
    }

    /**
     * training
     *
     * @throws IOException
     */
    public static void cartPole() throws IOException {
        DataManager manager = new DataManager(true);

        //define the mdp from gym (name, render)
        GymEnv<Box, Integer, DiscreteSpace> mdp = null;
        try {
            mdp = new GymEnv("CartPole-v0", false, false);
        } catch (RuntimeException e) {
            System.out.print("To run this example, download and start the gym-http-api repo found at https://github.com/openai/gym-http-api.");
        }
        //define the training
        QLearningDiscreteDense<Box> dql = new QLearningDiscreteDense(mdp, CARTPOLE_NET, CARTPOLE_QL, manager);

        //train
        dql.train();

        //get the final policy
        DQNPolicy<Box> pol = dql.getPolicy();

        //serialize and save (serialization showcase, but not required)
        pol.save("data/pol1");

        //close the mdp (close http)
        mdp.close();
    }

    /**
     * showcase serialization by using the trained agent on a new similar mdp (but render it this time)
     *
     * @throws IOException
     */
    public static void loadCartpole() throws IOException {
        //define the mdp from gym (name, render)
        GymEnv mdp2 = new GymEnv("CartPole-v0", true, false);

        //load the previous agent
        DQNPolicy<Box> pol2 = DQNPolicy.load("data/pol1");

        //evaluate the agent
        double rewards = 0;
        for (int i = 0; i < 1000; i++) {
            mdp2.reset();
            double reward = pol2.play(mdp2);
            rewards += reward;
            Logger.getAnonymousLogger().info("[" + (i + 1) + "] " + "Reward" + ": " + reward);
        }

        Logger.getAnonymousLogger().info("average: " + rewards / 1000);
    }
}
