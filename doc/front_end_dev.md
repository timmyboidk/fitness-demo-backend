# 前端开发指南 (Frontend Development Guide & SOP)

**文档目标:** 本文档兼具**架构设计参考**与**落地执行手册**的功能。
---

## 1. 核心架构设计 (Architecture Design)

### 1.1 设计理念
我们致力于打造一款在**中国大陆市场广泛兼容**且具有**旗舰级性能**的 AI 健身应用。
*   **兼容性优先:** 覆盖 5 年内发布的主流 Android (HarmonyOS/MIUI/ColorOS) 及 iOS 设备。
*   **计算下沉:** 所有的 AI 推理（含 3D 重建）必须在**端侧 (On-Device)** 完成，保障用户隐私与低延迟体验。
*   **双线程模型:** 严格分离 **UI 线程** (React/Layout) 与 **计算线程** (Worklet/Skia)，防止高负载推理导致掉帧。

### 1.2 技术栈选型 (Tech Stack)
| 模块          | 选型                         | 版本   | 选型理由                                                                |
| :------------ | :--------------------------- | :----- | :---------------------------------------------------------------------- |
| **Runtime**   | React Native                 | 0.74+  | 必须开启 **New Architecture (Fabric)** 以支持 JSI 同步调用。            |
| **Camera**    | `react-native-vision-camera` | v4.0+  | v4 提供了 Skia 集成与更稳定的 Frame Processor 调度。                    |
| **AI Engine** | `onnxruntime-react-native`   | v1.17+ | 这是**集成供应模型**的核心。支持 iOS CoreML 与 Android NNAPI 硬件加速。 |
| **3D Engine** | `@react-three/fiber`         | v8.x   | 声明式 3D 编程，配合 `drei/native` 实现高性能 GLB 加载。                |
| **Bridge**    | `react-native-worklets-core` | v1.x   | 实现 JS 线程与 Frame Processor 线程间的零拷贝通信。                     |

### 1.3 数据流架构 (Data Flow Pipeline)
**(核心链路)**
1.  **Capture:** Camera @ 720p 30fps -> YUV Frame Buffer.
2.  **Off-Screen Compute (Worklet Thread):**
    *   **Preprocess:** YUV -> RGB -> Resize (256x256) -> Normalize (mean/std).
    *   **Inference:** `ONNX Session.run()` -> 获取 `[1, 24, 3, 3]` 旋转矩阵张量。
    *   **Filter:** OneEuro Filter 平滑处理，消除抖动。
3.  **Sync:** 更新 Reanimated `SharedValue` (线程安全)。
4.  **Render (UI Thread):**
    *   `useFrame` (R3F loop) 读取 `SharedValue`。
    *   直接操作 `three.js` 骨骼节点 (`bone.quaternion.set()`)。
    *   **禁止** 通过 React State (`useState`) 传递姿态数据，否则会导致 GC 卡顿。

---

## 2. 外部模型集成指南 (Supplied Model Integration)

**这是集成算法团队交付的 `.onnx` / `.tflite` 模型的标准流程。**

### 2.1 模型文件安置
*   **路径:** `/assets/models/`
*   **命名规范:** `pose_estimator_v{version}_{platform}.onnx`
    *   Example: `pose_estimator_v1.2_ios.onnx` (FP16), `pose_estimator_v1.2_android.onnx` (INT8).

### 2.2 加载与初始化 (SOP)
在 `services/pipeline/ModelService.ts` 中实现：
```typescript
import { InferenceSession } from 'onnxruntime-react-native';

const modelPath = require('../../assets/models/pose_estimator.onnx');

// 1. 加载模型 (只需执行一次)
export async function loadModel() {
  const session = await InferenceSession.create(modelPath, {
    executionProviders: ['CoreML'], // Android 使用 ['NNAPI']
    graphOptimizationLevel: 'all',
  });
  return session;
}
```

### 2.3 张量绑定与推理 (Tensor Binding)
在 Frame Processor 中执行 (参考 `model_requirements_spec.md` 定义的 Input/Output Name):
```typescript
// 2. 推理循环
const results = await session.run({
  'input_rgb': new Tensor('float32', float32Data, [1, 256, 256, 3]) 
});

// 3. 解析输出
const poseOutput = results['output_pose']; // [1, 17, 3] or SMPL params
updateSharedValue(poseOutput.data);
```

### 2.4 模型热更新 (Model OTA Strategy)
配合后端 `/api/core/models/latest` 接口实现的静默更新策略。

**实现 `services/pipeline/ModelUpdater.ts`:**
```typescript
import * as FileSystem from 'expo-file-system';
import { checkVersion } from '../api/models';

export async function checkAndUpdateModel() {
  // 1. 检查云端版本
  const { hasUpdate, data } = await checkVersion('ios', currentLocalVersion);
  if (!hasUpdate) return;

  // 2. 下载新模型到应用沙盒
  const downloadRes = await FileSystem.downloadAsync(
    data.downloadUrl,
    FileSystem.documentDirectory + 'pose_latest.onnx'
  );

  // 3. 校验 MD5 (关键步骤，防篡改)
  const checksum = await FileSystem.getInfoAsync(downloadRes.uri, { md5: true });
  if (checksum.md5 === data.md5) {
    // 4. 重启推理 Session 加载新路径
    await reloadInferenceSession(downloadRes.uri);
  }
}
```

---

## 3. 环境与工程 搭建 SOP (Environment Setup)

### 3.1 基础安全与网络层
**Token 存储:** 使用 `expo-secure-store` 存储 JWT，禁止使用 Async Storage。
**请求拦截:**
```typescript
// services/api/client.ts
client.interceptors.request.use(async (config) => {
  const token = await SecureStore.getItemAsync('user_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
```

### 3.2 初始化项目
使用 Expo SDK 50+ (支持 New Architecture):
```bash
npx create-expo-app@latest fitness-app --template default
cd fitness-app
# 启用 New Architecture (Fabric)
npx expo prebuild --clean
```

### 3.2 安装核心依赖
复制以下命令执行，安装 3D、相机、手势与高性能组件：
```bash
# 基础 UI 与路由
npm install expo-router react-native-safe-area-context react-native-screens expo-linking expo-constants expo-status-bar

# 3D 引擎 (React Three Fiber)
npm install three @types/three @react-three/fiber @react-three/drei

# 原生高性能组件
npm install react-native-reanimated react-native-gesture-handler
npm install react-native-vision-camera
npm install react-native-worklets-core
npm install onnxruntime-react-native
```

### 3.3 配置文件修正
**修改 `babel.config.js`** 以支持 Reanimated 和 Worklets:
```javascript
module.exports = function(api) {
  api.cache(true);
  return {
    presets: ['babel-preset-expo'],
    plugins: [
      // 必须在最后
      'react-native-reanimated/plugin',
      ['react-native-worklets-core/plugin']
    ],
  };
};
```

---

## 4. 目录结构规范 (Project Structure)
所有代码必须严格遵循以下路径，禁止随意新增根目录文件夹。
```text
/app
  /_layout.tsx          # 根路由配置 (Stack)
  /index.tsx            # 启动页
  /(tabs)               # 底部导航栏
    /_layout.tsx
    /home.tsx
    /profile.tsx
  /Login.tsx            # 登录页
/components
  /3d                   # R3F 组件
    /HumanAvatar.tsx    # 3D 人偶组件 (加载 GLB)
    /Scene.tsx          # 灯光与环境
  /ui                   # 通用 UI (Button, Card)
/services
  /api                  #后端接口定义
    /auth.ts
    /scoring.ts
  /pipeline             # Vision Camera Frame Processors
    /ModelService.ts    # ONNX 加载与热更逻辑
    /pose-detector.ts   # 具体的推理 Worklet
/assets
  /models               # 存放 .onnx 和 .glb
/hooks
  /use-pose-estimation.ts # 封装推理 Hook
```

---

## 5. 风险控制与降级策略 (Risk Management)

### 5.1 发热与降频 (Thermal Throttling)
*   **现象:** 设备连续运行 15 分钟后，FPS 从 30 跌至 15。
*   **对策:**
    *   **Throttle:** 在 Frame Processor 中增加计数器，每 2 帧推理一次 (15fps Inference)，中间帧使用上一帧结果或线性插值。
    *   **Low Power Mode:** 检测到 `ProcessInfo.thermalState >= serious` (iOS API) 时，自动降低 R3F 渲染分辨率 (`dpr={0.5}`)。

### 5.2 内存泄漏 (Memory Leak)
*   **风险:** `Tensor` 对象未手动释放导致 OOM。
*   **规范:** 在 `session.run()` 后，必须显式调用 `tensor.dispose()` (如果库支持) 或依赖 Worklet 的 Scope 自动回收。必须使用 Xcode Instruments 定期通过 Leak Checks。

---

## 6. 功能开发 SOP (Implementation Steps)

### 6.1 3D 人偶加载 (Human Avatar)
**Step 1:** 将 `smpl_basic.glb` 放入 `assets/models/`。
**Step 2:** 创建 `components/3d/HumanAvatar.tsx`:
```tsx
import React, { useRef } from 'react';
import { useGLTF } from '@react-three/drei/native';
import { useFrame } from '@react-three/fiber';

export function HumanAvatar({ poseSharedValue }) {
  const { nodes, materials } = useGLTF(require('../../assets/models/smpl_basic.glb'));
  const group = useRef();

  useFrame(() => {
    // 从 SharedValue 读取旋转矩阵并应用给骨骼
    if (!poseSharedValue.value) return;
    const rotations = poseSharedValue.value; 
    // Example: nodes.Spine.quaternion.set(...)
    // 注意: 这里必须直接操作 threejs 对象，不仅 state update 以保证 30fps
  });

  return (
    <group ref={group} dispose={null}>
      <primitive object={nodes.root} />
      <skinnedMesh
        geometry={nodes.Mesh.geometry}
        material={materials.Skin}
        skeleton={nodes.Mesh.skeleton}
      />
    </group>
  );
}
```

### 6.2 相机推理管道 (Pose Pipeline)
**Step 1:** 创建 `services/pipeline/pose-detector.ts`:
```tsx
import { useFrameProcessor } from 'react-native-vision-camera';
import { runOnJS } from 'react-native-reanimated';

export function usePoseProcessor(onPoseDetected) {
  return useFrameProcessor((frame) => {
    'worklet'
    // 1. Preprocess: Resize & Normalize
    // 2. Inference: session.run(input)
    // 3. Update SharedValue (Fast path) or Call JS (Slow path)
    // console.log(`Frame: ${frame.width}x${frame.height}`)
  }, []);
}
```

### 6.3 数据采集服务 (Data Collection SOP)
文件: `services/analytics/DataCollector.ts`

此服务负责全量数据的本地缓冲、隐私脱敏与策略上传。

**核心逻辑:**
1.  **Buffer:** 写入 SQLite/MMKV 队列，防止数据丢失。
2.  **Sanitize:** 坐标抖动 + 去标识化。
3.  **Upload:** 仅在 Wi-Fi 下上传完整骨骼数据，4G 下仅上传统计 Key-Value。

```typescript
import NetInfo from "@react-native-community/netinfo";
import { v4 as uuidv4 } from 'uuid';

interface LogItem {
  sessionId: string;
  type: 'action_score' | 'app_event';
  payload: any;
  timestamp: number;
}

class DataCollector {
  private buffer: LogItem[] = [];
  private readonly BATCH_SIZE = 20;

  // 1. 采集入口
  public track(type: string, data: any) {
    const item: LogItem = {
      sessionId: global.currentSessionId,
      type,
      payload: this.sanitize(data), // 隐私脱敏
      timestamp: Date.now()
    };
    this.buffer.push(item);
    if (this.buffer.length >= this.BATCH_SIZE) {
      this.flush();
    }
  }

  // 2. 隐私脱敏 (Local Differential Privacy)
  private sanitize(data: any) {
    if (data.keypoints) {
      // 对关键点坐标加入微小高斯噪声 (+- 0.5px) 防止指纹识别
      data.keypoints = data.keypoints.map(k => ({
        x: k.x + (Math.random() - 0.5) * 1.0, 
        y: k.y + (Math.random() - 0.5) * 1.0,
        score: k.score
      }));
    }
    // 移除设备ID等敏感字段
    delete data.deviceId;
    return data;
  }

  // 3. 策略上传
  private async flush() {
    const state = await NetInfo.fetch();
    const isWifi = state.type === 'wifi';
    
    // 4G 环境下过滤掉大体积的骨骼数据
    const payload = this.buffer.filter(item => {
      if (isWifi) return true;
      return !item.payload.keypoints; // 4G 仅传行为 Stats
    });

    if (payload.length === 0) return;

    try {
      await fetch('https://api.fitness.com/api/data/collect', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ items: payload })
      });
      this.buffer = []; // Clear on success
    } catch (e) {
      // 失败保留在 Buffer (实际生产应持久化到 SQLite)
      console.warn('Upload failed, retrying next batch');
    }
  }
}

export const Collector = new DataCollector();
```

---

## 7. 兼容性与发布检查 (Compatibility Checklist)

### 7.1 Android `android/build.gradle`
为了兼容国产 ROM 严格的权限，必须在 `build.gradle` 中增加:
```gradle
buildscript {
    ext {
        minSdkVersion = 24  // Android 7.0+
        compileSdkVersion = 34
        targetSdkVersion = 34
        // 强制使用国内镜像加快构建
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/public' }
    }
}
```

### 4.3 隐私合规 (Privacy)
在 App 首次启动时，**必须** 弹窗展示《用户协议》和《隐私政策》，用户点击“同意”前：
1.  **禁止** 初始化 `react-native-vision-camera`。
2.  **禁止** 读取 `DeviceId` 或 `MacAddress`。
3.  **禁止** 发送任何网络请求至后端日志服务。

### 4.4 社交分享与登录 (Social Integration)
使用 `react-native-wechat-lib` 实现微信交互。
**Setup:**
```bash
npm install react-native-wechat-lib
```
**Code:**
```tsx
import * as WeChat from 'react-native-wechat-lib';

// App Launch
WeChat.registerApp('wx_app_id', 'universal_link');

// Login
async function wechatLogin() {
  const { code } = await WeChat.sendAuthRequest({ scope: 'snsapi_userinfo' });
  // Call Backend: POST /api/auth { type: 'login_wechat', payload: { code } }
}
```

---

## 5. 埋点规范 (Analytics SOP)

所有用户交互必须手动打点。
**代码模板:**
```tsx
import { Analytics } from '@/services/analytics';

// Component
<Button onPress={() => {
    Analytics.track('click_start_workout', { 
        sess_id: '...', 
        device_level: 'high' 
    });
    // ... business logic
}} />
```
