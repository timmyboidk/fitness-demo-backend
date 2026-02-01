/**
 * k6 负载测试脚本
 * 
 * 测试 Fitness 健身平台后端 API 的性能和稳定性。
 * 
 * 测试场景：
 * 1. 用户登录 - 模拟手机号登录
 * 2. 获取训练库 - 查询初级难度的训练内容
 * 
 * 负载配置：
 * - 2 分钟内逐步增加到 1000 并发用户
 * - 保持 1000 并发用户 1 分钟
 * - 1 分钟内逐步降到 0
 * 
 * 使用方式：
 *   k6 run load-test.js
 *   k6 run --env BASE_URL=http://api.example.com load-test.js
 * 
 * @author fitness-team
 * @since 1.0.0
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import exec from 'k6/execution';

/**
 * 负载测试配置
 * 使用阶梯式压力模型：爬坡 -> 稳定 -> 降压
 */
export const options = {
    stages: [
        { duration: '2m', target: 1000 },  // 2 分钟内增加到 1000 用户
        { duration: '1m', target: 1000 },  // 保持 1000 用户 1 分钟
        { duration: '1m', target: 0 },     // 1 分钟内降到 0
    ],
};

// API 基础地址，支持通过环境变量覆盖
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

/**
 * 主测试函数
 * 每个虚拟用户执行的测试流程
 */
export default function () {
    // ============ 步骤 1: 用户登录 ============
    // 使用虚拟用户 ID 和迭代次数生成唯一手机号
    const loginPayload = JSON.stringify({
        type: 'login_phone',
        phone: `139${exec.vu.idInTest.toString().padStart(4, '0')}${exec.scenario.iterationInTest.toString().padStart(4, '0')}`
    });

    // 生成随机 IP 地址，模拟不同客户端
    const fakeIp = `${Math.floor(Math.random() * 255) + 1}.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}`;
    const params = {
        headers: {
            'Content-Type': 'application/json',
            'X-Forwarded-For': fakeIp,  // 绕过 IP 限流
        },
    };

    // 发起登录请求
    const loginRes = http.post(`${BASE_URL}/api/auth`, loginPayload, params);

    // 验证登录响应
    check(loginRes, {
        '登录接口状态码 200': (r) => r.status === 200,
        '登录成功标志为 true': (r) => r.json('success') === true,
    });

    // ============ 步骤 2: 获取训练库 ============
    if (loginRes.status === 200 && loginRes.json('success')) {
        // 从登录响应中提取 JWT Token
        const token = loginRes.json('data.token');
        const authParams = {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,  // 携带认证 Token
            },
        };

        // 请求训练库列表
        const libRes = http.get(`${BASE_URL}/api/library?difficulty=novice`, authParams);
        
        // 验证训练库响应
        check(libRes, {
            '训练库接口状态码 200': (r) => r.status === 200,
            '训练库成功标志为 true': (r) => r.json('success') === true,
        });
    } else {
        // 登录失败时打印错误信息便于排查
        console.log(`登录失败: 状态码=${loginRes.status}, 响应=${loginRes.body}`);
    }
    
    // 请求间隔 1 秒，模拟真实用户行为
    sleep(1);
}
