// k6/farm-load-test.js
import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = 'http://localhost:8080';

// 미리 회원가입해둔 테스트 계정으로 바꿔줘
const TEST_EMAIL = 'loadtest@farmlink.com';
const TEST_PASSWORD = 'test1234!';

export const options = {
  scenarios: {
    farm_tps: {
      executor: 'ramping-arrival-rate',
      startRate: 10,          // 초당 10건부터 시작
      timeUnit: '1s',
      preAllocatedVUs: 50,    // 미리 준비해둘 가상유저 풀
      maxVUs: 200,            // 필요하면 최대 이만큼까지 늘림
      stages: [
        { target: 10, duration: '30s' }, // TPS 10 유지
        { target: 20, duration: '1m' },  // TPS 20까지 램프업 후 유지
        { target: 20, duration: '1m' },
        { target: 0, duration: '30s' },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],   // 실패율 1% 미만이어야 통과
    http_req_duration: ['p(95)<500'], // 95%가 500ms 이내여야 통과
  },
};

export function setup() {
  const res = http.post(
    `${BASE_URL}/api/users/login`,
    JSON.stringify({ email: TEST_EMAIL, password: TEST_PASSWORD }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  check(res, { '로그인 성공': (r) => r.status === 200 });
  return { token: res.json('accessToken') };
}

export default function (data) {
  const headers = { Authorization: `Bearer ${data.token}` };
  const res = http.get(`${BASE_URL}/api/cows`, { headers });
  check(res, { '개체 목록 조회 200': (r) => r.status === 200 });
}
