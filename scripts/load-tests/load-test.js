import http from 'k6/http';
import { check, sleep } from 'k6';
import exec from 'k6/execution';

export const options = {
    stages: [
        { duration: '2m', target: 1000 },
        { duration: '1m', target: 1000 },
        { duration: '1m', target: 0 },
    ],
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
    // 1. Login
    const loginPayload = JSON.stringify({
        type: 'login_phone',
        phone: `139${exec.vu.idInTest.toString().padStart(4, '0')}${exec.scenario.iterationInTest.toString().padStart(4, '0')}`
    });

    const fakeIp = `${Math.floor(Math.random() * 255) + 1}.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}`;
    const params = {
        headers: {
            'Content-Type': 'application/json',
            'X-Forwarded-For': fakeIp,
        },
    };

    const loginRes = http.post(`${BASE_URL}/api/auth`, loginPayload, params);

    check(loginRes, {
        'login status is 200': (r) => r.status === 200,
        'login success is true': (r) => r.json('success') === true,
    });

    if (loginRes.status === 200 && loginRes.json('success')) {
        const token = loginRes.json('data.token');
        const authParams = {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
        };

        // 2. Get Library
        const libRes = http.get(`${BASE_URL}/api/library?difficulty=novice`, authParams);
        check(libRes, {
            'library status is 200': (r) => r.status === 200,
            'library success is true': (r) => r.json('success') === true,
        });
    } else {
        console.log(`Login Failed: Status=${loginRes.status}, Body=${loginRes.body}`);
    }
    sleep(1);
}
