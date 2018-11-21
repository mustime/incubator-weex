/*************************************
 * @Author  Irvin Pang @ XXTeam
 * @E-mail  halo.irvin@gmail.com
 *************************************/

#ifndef WEEXCORE_WEEX_CALLBACK_MANAGER_H
#define WEEXCORE_WEEX_CALLBACK_MANAGER_H

#include <map>
#include <string>
#include <functional>

namespace WeexCore {

    typedef std::function<void(const std::string&, const std::string&, const std::string&)> Callback;

    class WeexCallbackManager {
    private:
        static WeexCallbackManager *m_pInstance;

        std::map<std::string, std::map<std::string, Callback>> _callbacks;

    private:
        WeexCallbackManager() {};

        ~WeexCallbackManager() {
        };

        //just to release singleton object
        class Garbo {
        public:
            ~Garbo() {
                if (WeexCallbackManager::m_pInstance) {
                    delete WeexCallbackManager::m_pInstance;
                }
            }
        };

        static Garbo garbo;

        std::map<std::string, Callback>* getCallbackMap(const std::string& instanceID, const std::string& event);

    public:
        static WeexCallbackManager *getInstance() {
            if (nullptr == m_pInstance) {
                m_pInstance = new WeexCallbackManager();
            }
            return m_pInstance;
        };

        void clear(const std::string& instanceID);
        void registerComponent(const std::string& instanceID, const std::string& ref, const std::string& event, const Callback& callback);
        void cancelRegister(const std::string& instanceID, const std::string& ref, const std::string& event);

        void postToComponent(const std::string& instanceID, const std::string& ref, const std::string& event);

    };
}

#endif //WEEXCORE_WEEX_CALLBACK_MANAGER_H
