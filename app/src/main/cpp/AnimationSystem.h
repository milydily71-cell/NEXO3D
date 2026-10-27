#pragma once
#include <array>
#include <cstdint>
#include <string>
#include <vector>

namespace nexo::animation {
struct Vec3 { float x=0, y=0, z=0; };
struct Quat { float x=0, y=0, z=0, w=1; };
struct Joint {
    std::string name;
    std::int32_t parent=-1;
    std::array<float,16> inverseBindMatrix{1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1};
};
struct Skeleton { std::vector<Joint> joints; };
template<class T> struct Key { float time=0; T value{}; };
struct JointTrack {
    std::uint32_t joint=0;
    std::vector<Key<Vec3>> translations;
    std::vector<Key<Quat>> rotations;
    std::vector<Key<Vec3>> scales;
};
struct Clip { std::string name; float duration=0; std::vector<JointTrack> tracks; };
Vec3 sample(const std::vector<Key<Vec3>>& keys, float time, Vec3 fallback={});
Quat sample(const std::vector<Key<Quat>>& keys, float time, Quat fallback={});
bool validate(const Skeleton& skeleton, const Clip& clip, std::string& error);
}
