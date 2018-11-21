/*************************************
 * @Author  Irvin Pang @ XXTeam
 * @E-mail  halo.irvin@gmail.com
 *************************************/

#include <vector>

#include "weex_callback_manager.h"

namespace WeexCore {

    WeexCallbackManager* WeexCallbackManager::m_pInstance = nullptr;

    std::map<std::string, Callback>* WeexCallbackManager::getCallbackMap(const std::string& instanceID, const std::string& event)
    {
        std::string key = instanceID + "@" + event;
        if (_callbacks.find(key) == _callbacks.end())
            _callbacks[key] = std::map<std::string, Callback>();
        return &_callbacks[key];
    }

    void WeexCallbackManager::clear(const std::string& instanceID)
    {
        std::string prefix = instanceID + "@";
        std::vector<std::string> pendingKeys;
        for (const auto& iter : _callbacks)
        {
            if (strncmp(iter.first.c_str(), prefix.c_str(), prefix.length()) == 0)
                pendingKeys.push_back(iter.first);
        }
        for (const auto& key : pendingKeys)
            _callbacks.erase(key);
    }

    void WeexCallbackManager::registerComponent(const std::string& instanceID, const std::string& ref, const std::string& event, const Callback& callback)
    {
        auto* callbacks = getCallbackMap(instanceID, event);
        callbacks->insert(std::make_pair(ref, callback));
    }

    void WeexCallbackManager::cancelRegister(const std::string& instanceID, const std::string& ref, const std::string& event)
    {
        auto* callbacks = getCallbackMap(instanceID, event);
        const auto& iter = callbacks->find(ref);
        if (iter != callbacks->end())
            callbacks->erase(iter);
    }

    void WeexCallbackManager::postToComponent(const std::string& instanceID, const std::string& ref, const std::string& event)
    {
        auto* callbacks = getCallbackMap(instanceID, event);
        const auto& iter = callbacks->find(ref);
        if (iter != callbacks->end())
            iter->second(instanceID, ref, event);
    }
}