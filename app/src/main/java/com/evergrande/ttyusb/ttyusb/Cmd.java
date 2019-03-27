package com.evergrande.ttyusb.ttyusb;

/**
 * @author by AllenJ on 2018/5/3.
 */

public interface Cmd {
    String TEST_CMD_VALUE = "0123456789ABCDEF";
    String TEST_CMD_FINISH = "TEST_FINISH";
    String TEXT_CMD_SUCCESS = "测试结果成功";
    String TEXT_CMD_FAILL = "测试结果失败";
    final int TEST_COUNT = 15;
}
