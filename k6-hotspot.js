import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 10,
    duration: '30s',
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const HOT_USER_ID = 7;
const HOT_TRAFFIC_RATIO = 0.8;
const COLD_USER_ID_START = 100;
const COLD_USER_COUNT = 30;

function pickUserId() {
    if (Math.random() < HOT_TRAFFIC_RATIO) {
        return HOT_USER_ID;
    }
    return COLD_USER_ID_START + Math.floor(Math.random() * COLD_USER_COUNT);
}

export default function () {
    const payload = JSON.stringify({
        userId: pickUserId(),
        amount: Math.round(Math.random() * 100000) / 100,
    });

    const response = http.post(`${BASE_URL}/transactions`, payload, {
        headers: { 'Content-Type': 'application/json' },
    });

    check(response, {
        'created': (r) => r.status === 201,
    });
}
