<template>
  <!-- 登录界面布局 -->
  <a-row class="login">
    <!-- 中央列设置，用于登录表单 -->
    <a-col :span="8" :offset="8" class="login-main">
      <!-- 登录标题和图标 -->
      <h1 style="text-align: center"><rocket-two-tone />&nbsp;12306售票系统</h1>
      <!-- 登录表单 -->
      <a-form
          :model="loginForm"
          name="basic"
          autocomplete="off"
      >
        <!-- 手机号输入框 -->
        <a-form-item
            label=""
            name="mobile"
            :rules="[{ required: true, message: '请输入手机号!' }]"
        >
          <a-input v-model:value="loginForm.mobile" placeholder="手机号"/>
        </a-form-item>

        <!-- 验证码输入框和获取验证码按钮 -->
        <a-form-item
            label=""
            name="code"
            :rules="[{ required: true, message: '请输入验证码!' }]"
        >
          <a-input v-model:value="loginForm.code">
            <template #addonAfter>
              <a @click="sendCode">获取验证码</a>
            </template>
          </a-input>
          <!--<a-input v-model:value="loginForm.code" placeholder="验证码"/>-->
        </a-form-item>

        <!-- 登录按钮 -->
        <a-form-item>
          <a-button type="primary" block @click="login">登录</a-button>
        </a-form-item>

      </a-form>
    </a-col>
  </a-row>
</template>

<script>
import { defineComponent, reactive } from 'vue';
import axios from "axios";
import {notification} from "ant-design-vue";
import { useRouter } from 'vue-router'
import store from "@/store";

export default defineComponent({
  name: "login-view",
  setup() {
    const router = useRouter();

    // 登录表单数据模型
    const loginForm = reactive({
      mobile: '13958858054',
      code: '',
    });

    /**
     * 发送验证码函数
     * 通过axios向后端发送请求，获取验证码
     */
    const sendCode = () => {
      axios.post("/member/member/send-code", {
        mobile: loginForm.mobile
      }).then(response => {
        let data = response.data; //response.data == 后端的CommonResp
        if (data.success) {
          notification.success({
            description: '验证码发送成功，请注意查收'
          })
        } else {
          notification.error({
            description: data.message
          });
        }
      });
    };

    /**
     * 登录函数
     * 通过axios向后端发送登录请求，验证用户身份
     */
    const login = () => {
      axios.post("/member/member/login", {
        mobile: loginForm.mobile,
        code: loginForm.code
      }).then(response => {
        let data = response.data; //response.data == 后端的CommonResp
        if (data.success) {
          notification.success({
            description: '登录成功！'
          })
          console.log("登录成功： ",data.content);
          router.push('/welcome'); // 登录成功，跳转到控制台主页
          store.commit('setMember',data.content)
        } else {
          notification.error({
            description: data.message
          });
        }
      });
    };


    return {
      loginForm,
      sendCode,
      login
    };
  },
});
</script>

<style>
/* 设置登录主要区域的样式 */
.login-main h1 {
  font-size: 25px;
  font-weight: bold;
}

.login-main {
  margin-top: 100px;
  padding: 30px 30px 20px;
  border: 2px solid grey;
  border-radius: 10px;
  background-color: #fcfcfc;
}
</style>
