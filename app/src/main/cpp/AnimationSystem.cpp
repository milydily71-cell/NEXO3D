#include "AnimationSystem.h"
#include <algorithm>
#include <cmath>

namespace nexo::animation {
namespace {
template<class T> std::size_t segment(const std::vector<Key<T>>& k,float t) {
    auto it=std::upper_bound(k.begin(),k.end(),t,[](float value,const Key<T>& key){return value<key.time;});
    if(it==k.begin()) return 0;
    return static_cast<std::size_t>(std::distance(k.begin(),it)-1);
}
float clamp01(float v){return std::max(0.0f,std::min(1.0f,v));}
Quat normalized(Quat q){float n=std::sqrt(q.x*q.x+q.y*q.y+q.z*q.z+q.w*q.w);if(n<1e-8f)return {};q.x/=n;q.y/=n;q.z/=n;q.w/=n;return q;}
}
Vec3 sample(const std::vector<Key<Vec3>>& keys,float time,Vec3 fallback){
    if(keys.empty())return fallback;if(keys.size()==1||time<=keys.front().time)return keys.front().value;if(time>=keys.back().time)return keys.back().value;
    auto i=segment(keys,time),j=i+1;float dt=keys[j].time-keys[i].time;float a=dt>1e-8f?clamp01((time-keys[i].time)/dt):0;
    const auto& p=keys[i].value;const auto& q=keys[j].value;return {p.x+(q.x-p.x)*a,p.y+(q.y-p.y)*a,p.z+(q.z-p.z)*a};
}
Quat sample(const std::vector<Key<Quat>>& keys,float time,Quat fallback){
    if(keys.empty())return fallback;if(keys.size()==1||time<=keys.front().time)return normalized(keys.front().value);if(time>=keys.back().time)return normalized(keys.back().value);
    auto i=segment(keys,time),j=i+1;float dt=keys[j].time-keys[i].time;float a=dt>1e-8f?clamp01((time-keys[i].time)/dt):0;
    Quat p=normalized(keys[i].value),q=normalized(keys[j].value);float dot=p.x*q.x+p.y*q.y+p.z*q.z+p.w*q.w;
    if(dot<0){dot=-dot;q={-q.x,-q.y,-q.z,-q.w};}
    if(dot>.9995f)return normalized({p.x+(q.x-p.x)*a,p.y+(q.y-p.y)*a,p.z+(q.z-p.z)*a,p.w+(q.w-p.w)*a});
    float theta=std::acos(std::max(-1.0f,std::min(1.0f,dot)));float sinTheta=std::sin(theta);if(std::abs(sinTheta)<1e-8f)return p;
    float wa=std::sin((1-a)*theta)/sinTheta,wb=std::sin(a*theta)/sinTheta;
    return normalized({p.x*wa+q.x*wb,p.y*wa+q.y*wb,p.z*wa+q.z*wb,p.w*wa+q.w*wb});
}
bool validate(const Skeleton& skeleton,const Clip& clip,std::string& error){
    if(skeleton.joints.size()>1024){error="Skeleton exceeds 1024 joints";return false;}
    for(std::size_t i=0;i<skeleton.joints.size();++i){auto p=skeleton.joints[i].parent;if(p>=static_cast<std::int32_t>(skeleton.joints.size())||p==static_cast<std::int32_t>(i)||p< -1){error="Invalid skeleton parent index";return false;}}
    for(const auto& track:clip.tracks){if(track.joint>=skeleton.joints.size()){error="Animation track joint index is out of range";return false;}
        auto validTimes=[&](const auto& keys){float last=-1.0f;for(const auto& key:keys){if(!std::isfinite(key.time)||key.time<0||key.time<last){return false;}last=key.time;}return true;};
        if(!validTimes(track.translations)||!validTimes(track.rotations)||!validTimes(track.scales)){error="Animation key times must be finite, non-negative and sorted";return false;}
    }
    error.clear();return true;
}
}
