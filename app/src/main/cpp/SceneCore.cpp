#include "SceneCore.h"
#include <algorithm>
#include <sstream>
namespace nexo {
SceneCore::SceneCore() { addCube(); undo_.clear(); redo_.clear(); }
void SceneCore::checkpoint() { undo_.push_back({entities_,selected_}); if(undo_.size()>64)undo_.erase(undo_.begin()); redo_.clear(); }
Entity* SceneCore::find(std::uint32_t id) { auto it=std::find_if(entities_.begin(),entities_.end(),[id](const Entity&e){return e.id==id;}); return it==entities_.end()?nullptr:&*it; }
const Entity* SceneCore::find(std::uint32_t id) const { auto it=std::find_if(entities_.begin(),entities_.end(),[id](const Entity&e){return e.id==id;}); return it==entities_.end()?nullptr:&*it; }
std::uint32_t SceneCore::addCube() { checkpoint(); Entity e; e.id=nextId_++; e.name="Cube_"+std::to_string(e.id); e.transform.position={static_cast<float>(entities_.size()%5)*2.4f,0,0}; selected_=e.id; entities_.push_back(e); return e.id; }
bool SceneCore::removeLast() { if(entities_.empty())return false; checkpoint(); entities_.pop_back(); selected_=entities_.empty()?0:entities_.back().id; return true; }
bool SceneCore::undo() { if(undo_.empty())return false; redo_.push_back({entities_,selected_}); auto s=std::move(undo_.back());undo_.pop_back();entities_=std::move(s.entities);selected_=s.selected;return true; }
bool SceneCore::redo() { if(redo_.empty())return false;undo_.push_back({entities_,selected_});auto s=std::move(redo_.back());redo_.pop_back();entities_=std::move(s.entities);selected_=s.selected;return true; }
int SceneCore::count() const { return static_cast<int>(entities_.size()); }
std::uint32_t SceneCore::selectedId() const { return selected_; }
bool SceneCore::setPosition(std::uint32_t id,Vec3 p){if(!find(id))return false;checkpoint();auto*e=find(id);e->transform.position=p;return true;}
bool SceneCore::addPositionKey(std::uint32_t id,int frame,Vec3 p){if(!find(id)||frame<0)return false;checkpoint();auto*e=find(id);auto it=std::find_if(e->positionKeys.begin(),e->positionKeys.end(),[frame](const Keyframe&k){return k.frame==frame;});if(it==e->positionKeys.end())e->positionKeys.push_back({frame,p});else it->position=p;std::sort(e->positionKeys.begin(),e->positionKeys.end(),[](const Keyframe&a,const Keyframe&b){return a.frame<b.frame;});return true;}
Vec3 SceneCore::samplePosition(std::uint32_t id,float frame) const {const auto*e=find(id);if(!e||e->positionKeys.empty())return e?e->transform.position:Vec3{};const auto&k=e->positionKeys;if(frame<=k.front().frame)return k.front().position;if(frame>=k.back().frame)return k.back().position;for(std::size_t i=1;i<k.size();++i)if(frame<=k[i].frame){float t=(frame-k[i-1].frame)/static_cast<float>(k[i].frame-k[i-1].frame);return {k[i-1].position.x+(k[i].position.x-k[i-1].position.x)*t,k[i-1].position.y+(k[i].position.y-k[i-1].position.y)*t,k[i-1].position.z+(k[i].position.z-k[i-1].position.z)*t};}return k.back().position;}
std::string SceneCore::serialize() const {std::ostringstream o;o.precision(7);o<<"{\"format\":\"nexo-scene-1\",\"selected\":"<<selected_<<",\"entities\":[";for(std::size_t i=0;i<entities_.size();++i){const auto&e=entities_[i];if(i)o<<',';o<<"{\"id\":"<<e.id<<",\"name\":\""<<e.name<<"\",\"type\":\"cube\",\"position\":["<<e.transform.position.x<<','<<e.transform.position.y<<','<<e.transform.position.z<<"],\"rotation\":["<<e.transform.rotation.x<<','<<e.transform.rotation.y<<','<<e.transform.rotation.z<<"],\"scale\":["<<e.transform.scale.x<<','<<e.transform.scale.y<<','<<e.transform.scale.z<<"],\"positionKeys\":[";for(std::size_t j=0;j<e.positionKeys.size();++j){const auto&k=e.positionKeys[j];if(j)o<<',';o<<"{\"frame\":"<<k.frame<<",\"value\":["<<k.position.x<<','<<k.position.y<<','<<k.position.z<<"]}";}o<<"]}";}o<<"]}";return o.str();}
}
