/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
#include <utility>
#include <vector>

#include "base/ViewUtils.h"
#include "core/css/constants_name.h"
#include "base/TimeUtils.h"
#include "core/layout/measure_func_adapter.h"
#include "core/render/manager/render_manager.h"
#include "core/render/node/render_object.h"
#include "core/render/page/render_page.h"

namespace WeexCore {

RenderManager *RenderManager::g_pInstance = nullptr;

bool RenderManager::CreateRenderObject(std::string page_id, const std::string& pathLayout, const std::string& pathStyle)
{
  RenderPage *page = new RenderPage(page_id);
  this->pages_.insert(std::pair<std::string, RenderPage *>(page_id, page));

  int64_t start_time = getCurrentTime();
  RenderObject *root = JsonFile2RenderObject(pathLayout, pathStyle, page_id);
  page->ParseJsonTime(getCurrentTime() - start_time);

  page->set_is_dirty(true);
  return page->CreateRootRender(root);
}

bool RenderManager::CreateRenderObject(std::string page_id, const Json::Value& jsonLayout, const Json::Value& jsonStyle)
{
  RenderPage *page = new RenderPage(page_id);
  this->pages_.insert(std::pair<std::string, RenderPage *>(page_id, page));

  int64_t start_time = getCurrentTime();
  RenderObject *root = Json2RenderObject(jsonLayout, jsonStyle, page_id);
  page->ParseJsonTime(getCurrentTime() - start_time);

  page->set_is_dirty(true);
  return page->CreateRootRender(root);
}

bool RenderManager::AddRenderObject(const std::string &page_id, const std::string &parentRef,
                                    int index, const Json::Value& jsonLayout, const Json::Value& jsonStyle)
{
  RenderPage *page = GetPage(page_id);
  if (page == nullptr) return false;

  int64_t start_time = getCurrentTime();
  RenderObject *child = Json2RenderObject(jsonLayout, jsonStyle, page_id);
  page->ParseJsonTime(getCurrentTime() - start_time);

  if (child == nullptr) return false;

  page->set_is_dirty(true);
  return page->AddRenderObject(parentRef, index, child);
}

bool RenderManager::RemoveRenderObject(const std::string &page_id, const std::string &ref)
{
  RenderPage *page = this->GetPage(page_id);
  if (page == nullptr) return false;

  page->set_is_dirty(true);
  return page->RemoveRenderObject(ref);
}

bool RenderManager::MoveRenderObject(const std::string &page_id,
                                     const std::string &ref,
                                     const std::string &parent_ref, int index) {
  RenderPage *page = this->GetPage(page_id);
  if (page == nullptr) return false;

  page->set_is_dirty(true);
  return page->MoveRenderObject(ref, parent_ref, index);
}

bool RenderManager::UpdateAttr(const std::string &page_id,
                               const std::string &ref, const Json::Value& json) {
  RenderPage *page = this->GetPage(page_id);
  if (page == nullptr) return false;

  int64_t start_time = getCurrentTime();
  std::vector<std::pair<std::string, std::string>> *attrs = Json2Pairs(json);
  page->ParseJsonTime(getCurrentTime() - start_time);

  page->set_is_dirty(true);
  return page->UpdateAttr(ref, attrs);
}

bool RenderManager::UpdateStyle(const std::string &page_id,
                                const std::string &ref, const Json::Value& json) {
  RenderPage *page = this->GetPage(page_id);
  if (page == nullptr) return false;

  int64_t start_time = getCurrentTime();
  std::vector<std::pair<std::string, std::string>> *styles = Json2Pairs(json);
  page->ParseJsonTime(getCurrentTime() - start_time);

  page->set_is_dirty(true);
  return page->UpdateStyle(ref, styles);
}


bool RenderManager::AddEvent(const std::string &page_id, const std::string &ref,
                             const std::string &event) {
  RenderPage *page = this->GetPage(page_id);
  if (page == nullptr) return false;

  page->set_is_dirty(true);
  return page->AddEvent(ref, event);
}

bool RenderManager::RemoveEvent(const std::string &page_id,
                                const std::string &ref,
                                const std::string &event) {
  RenderPage *page = this->GetPage(page_id);
  if (page == nullptr) return false;

  page->set_is_dirty(true);
  return page->RemoveEvent(ref, event);
}

bool RenderManager::CreateFinish(const std::string &page_id) {
  RenderPage *page = GetPage(page_id);
  if (page == nullptr) return false;

  page->set_is_dirty(true);
  return page->CreateFinish();
}

bool RenderManager::CallNativeModule(const char *page_id, const char *module, const char *method,
                                     const char *arguments, int argumentsLength,
                                     const char *options, int optionsLength) {
  if (strcmp(module, "meta") == 0) {
    CallMetaModule(method, arguments);
  }
}

bool RenderManager::CallMetaModule(const char *method, const char *arguments) {

  if (strcmp(method, "setViewport") == 0) {
//    wson_parser parser(arguments);
//    if (parser.isArray(parser.nextType())) {
//      int size = parser.nextArraySize();
//      for (int i = 0; i < size; i++) {
//        uint8_t value_type = parser.nextType();
//        if (parser.isMap(value_type)) {
//          int map_size = parser.nextMapSize();
//          for (int j = 0; j < map_size; j++) {
//            std::string key = parser.nextMapKeyUTF8();
//            std::string value = parser.nextStringUTF8(parser.nextType());
//            if (strcmp(key.c_str(), WIDTH) == 0) {
//              RenderManager::GetInstance()->set_viewport_width(getFloat(value.c_str()));
//            }
//          }
//        }
//      }
//    }
  }
}

RenderPage *RenderManager::GetPage(const std::string &page_id) {
  std::map<std::string, RenderPage *>::iterator iter =
      this->pages_.find(page_id);
  if (iter != this->pages_.end()) {
    return iter->second;
  } else {
    return nullptr;
  }
}

bool RenderManager::ClosePage(const std::string &page_id) {
  RenderPage *page = GetPage(page_id);
  if (page == nullptr) return false;

  page->OnRenderPageClose();
  this->pages_.erase(page_id);
  delete page;
  page = nullptr;
}

void RenderManager::Batch(const std::string &page_id) {
  RenderPage *page = this->GetPage(page_id);
  if (page == nullptr) return;

  page->Batch();
}

}  // namespace WeexCore
