package com.statemode;

public interface OilTankState {
    /**
     * 开始注油
     */
    public void chargingStart() throws StateException;

    /**
     * 结束注油
     *
     * @throws StateException
     */
    public void chargingEnd() throws StateException;

    /**
     * 开始供油
     */
    public void feedingStart() throws StateException;

    /**
     * 结束供油
     */
    public void feedingEnd() throws StateException;

    /**
     * 结束驻留
     */
    public void stayEnd() throws StateException;

    /**
     * 开始加热
     *
     * @throws StateException
     */
    public void hotStart() throws StateException;

    /**
     * 结束加热
     *
     * @throws StateException
     */
    public void hotEnd() throws StateException;
}
