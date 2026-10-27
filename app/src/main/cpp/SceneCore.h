#pragma once
#include <cstdint>
#include <string>
#include <vector>
namespace nexo {
struct Vec3 { float x=0, y=0, z=0; };
struct Transform { Vec3 position{}; Vec3 rotation{}; Vec3 scale{1,1,1}; };
struct Keyframe { int frame=0; Vec3 position{}; };
struct Entity { std::uint32_t id=0; std::string name; Transform transform{}; std::vector<Keyframe> positionKeys; };
class SceneCore {
 public:
  SceneCore();
  std::uint32_t addCube(); bool removeLast(); bool undo(); bool redo();
  int count() const; std::uint32_t selectedId() const;
  bool setPosition(std::uint32_t id, Vec3 position);
  bool addPositionKey(std::uint32_t id, int frame, Vec3 position);
  Vec3 samplePosition(std::uint32_t id, float frame) const;
  std::string serialize() const;
 private:
  struct Snapshot { std::vector<Entity> entities; std::uint32_t selected=0; };
  std::vector<Entity> entities_; std::vector<Snapshot> undo_, redo_;
  std::uint32_t nextId_=1, selected_=0;
  void checkpoint(); Entity* find(std::uint32_t id); const Entity* find(std::uint32_t id) const;
};
}
