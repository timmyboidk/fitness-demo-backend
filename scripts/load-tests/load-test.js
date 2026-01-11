import http from 'k6/http';
import { check, sleep } from 'k6';
import exec from 'k6/execution';

export const options = {
    stages: [
        { duration: '30s', target: 5 }, // Ramp to 5 users
        { duration: '1m', target: 5 }, // Stay at 5
        { duration: '10s', target: 0 }, // Ramp down
    ],
};

const BASE_URL = 'http://fitness-backend:8080'; // Use service name in Docker network

export default function () {
    // 1. Login
    const loginPayload = JSON.stringify({
        type: 'login_phone',
        phone: `139${exec.vu.idInTest.toString().padStart(4, '0')}${exec.scenario.iterationInTest.toString().padStart(4, '0')}`
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
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
