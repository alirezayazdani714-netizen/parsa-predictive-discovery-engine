"""
PARSA LAYER ACCESS CONTROL (PHASE 3 HARDENED)
=============================================
Enforces real code-level access boundaries across all 7 layers.
Prohibits unauthorized cross-layer reads, writes, and look-ahead.
"""

from typing import Optional, Set, Dict, Callable, Any
import functools
import threading
from parsa_layers.contracts.models import UnauthorizedLayerAccessViolation

ALLOWED_READ_MATRIX: Dict[str, Set[str]] = {
    "SCENARIO": {"SCENARIO"},
    "LABORATORY": {"SCENARIO", "LABORATORY"},
    "EXECUTOR": {"SCENARIO", "LABORATORY", "EXECUTOR"},
    "TEST": {"SCENARIO", "EXECUTOR", "TEST"},
    "JUDGES": {"TEST", "JUDGES"},
    "REPORT": {"JUDGES", "TEST", "GUARDIAN", "REPORT"},
    "GUARDIAN": {"SCENARIO", "LABORATORY", "EXECUTOR", "TEST", "JUDGES", "REPORT", "GUARDIAN"}
}

ALLOWED_WRITE_MATRIX: Dict[str, Set[str]] = {
    "SCENARIO": {"SCENARIO"},
    "LABORATORY": {"LABORATORY"},
    "EXECUTOR": {"EXECUTOR"},
    "TEST": {"TEST"},
    "JUDGES": {"JUDGES"},
    "REPORT": {"REPORT"},
    "GUARDIAN": {"GUARDIAN"}
}

_thread_local = threading.local()


class LayerAccessController:
    """Centralized runtime access gatekeeper."""

    @classmethod
    def get_current_layer(cls) -> Optional[str]:
        return getattr(_thread_local, "current_layer", None)

    @classmethod
    def set_current_layer(cls, layer_name: Optional[str]) -> None:
        _thread_local.current_layer = layer_name

    @classmethod
    def check_access(cls, caller_layer: str, target_layer: str, operation: str = "READ") -> None:
        caller = caller_layer.upper()
        target = target_layer.upper()
        op = operation.upper()

        if op == "READ":
            allowed_targets = ALLOWED_READ_MATRIX.get(caller, set())
            if target not in allowed_targets:
                raise UnauthorizedLayerAccessViolation(
                    f"CRITICAL ACCESS VIOLATION: Layer '{caller}' is forbidden from reading layer '{target}'. Allowed: {sorted(list(allowed_targets))}"
                )
        elif op == "WRITE":
            allowed_targets = ALLOWED_WRITE_MATRIX.get(caller, set())
            if target not in allowed_targets:
                raise UnauthorizedLayerAccessViolation(
                    f"CRITICAL ACCESS VIOLATION: Layer '{caller}' is forbidden from writing to layer '{target}'. Allowed: {sorted(list(allowed_targets))}"
                )
        else:
            raise ValueError(f"Unknown access operation '{operation}'. Must be READ or WRITE.")

    @classmethod
    def enforce_read(cls, target_layer: str) -> None:
        current = cls.get_current_layer()
        if current is not None:
            cls.check_access(current, target_layer, "READ")

    @classmethod
    def enforce_write(cls, target_layer: str) -> None:
        current = cls.get_current_layer()
        if current is not None:
            cls.check_access(current, target_layer, "WRITE")


class LayerContext:
    """Context manager for executing within a specific architectural layer boundary."""

    def __init__(self, layer_name: str):
        self.layer_name = layer_name.upper()
        self._previous_layer: Optional[str] = None

    def __enter__(self):
        self._previous_layer = LayerAccessController.get_current_layer()
        LayerAccessController.set_current_layer(self.layer_name)
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        LayerAccessController.set_current_layer(self._previous_layer)


def enforce_layer(layer_name: str) -> Callable:
    """Decorator to enforce layer context during method execution."""
    def decorator(func: Callable) -> Callable:
        @functools.wraps(func)
        def wrapper(*args, **kwargs) -> Any:
            with LayerContext(layer_name):
                return func(*args, **kwargs)
        return wrapper
    return decorator
